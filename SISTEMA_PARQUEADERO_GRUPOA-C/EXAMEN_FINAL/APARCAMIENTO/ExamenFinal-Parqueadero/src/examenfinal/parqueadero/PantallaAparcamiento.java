/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package examenfinal.parqueadero;

import examenfinal.parqueadero.dto.RegistroAparcamientoDto;
import examenfinal.parqueadero.dto.VehiculoDto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PantallaAparcamiento extends javax.swing.JPanel {

    /**
     * Creates new form PantallaAparcamiento
     */
    public PantallaAparcamiento() {
        initComponents();
        listarAparcamientos();
    }
    
    public void cargarTarifa(){

    }

    private VehiculoDto getVehiculo(String placa){
        ConexionBaseDatos conexion = new ConexionBaseDatos();
        Connection connection = conexion.conectar();
        try {
            // Usamos PreparedStatement para evitar SQL Injection
            String query = "SELECT id FROM vehiculo WHERE placa LIKE ?";

            // Preparamos la consulta
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Establecemos el valor del parámetro (placa) en la consulta
            preparedStatement.setString(1, placa); // 1 es el índice del primer parámetro

            // Ejecutamos la consulta
            ResultSet result = preparedStatement.executeQuery();

            // Procesamos el resultado
            if (result.next()) {
                // Aquí puedes mapear el resultado al DTO
                VehiculoDto vehiculo = new VehiculoDto();
                vehiculo.setId(result.getInt("id"));
                // Agrega más campos según lo que necesites del ResultSet
                return vehiculo;
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Puedes gestionar el error según tu necesidad
        } finally {
            try {
                // Cerramos la conexión (o puedes usar un pool de conexiones para optimizar)
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        JOptionPane.showMessageDialog(null, "Vehículo no encontrado.");
        return null;  // Si no se encuentra el vehículo
    }

    public boolean RegistrarEntradaAparcamiento(String placa) {
        ConexionBaseDatos conexion = new ConexionBaseDatos();
        Connection connection = null;
        PreparedStatement pst = null;

        try {
            connection = conexion.conectar();

            VehiculoDto vehiculo = getVehiculo(placa);

            // Validamos si el vehículo se encuentra registrado
            if (vehiculo == null) {
                return false;
            }

            String sql = "INSERT INTO aparcamiento " +
                    "(vehiculo_id, tiempo_total, monto_total, fecha_entrada, fecha_salida, activo) " +
                    "VALUES(?, 0, 0, ?, null, true);";

            pst = connection.prepareStatement(sql);
            pst.setInt(1, vehiculo.getId());

            // Establecer la fecha de entrada
            LocalDateTime fechaEntrada = LocalDateTime.now();
            pst.setTimestamp(2, Timestamp.valueOf(fechaEntrada));

            int result = pst.executeUpdate();

            if (result != 0) {
                JOptionPane.showMessageDialog(null, "Vehículo con placa " + placa
                        + " ingresado con fecha y hora " + fechaEntrada.toString());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error registrando el vehículo: " + e);
            return false;
        } finally {
            // Asegurarse de cerrar los recursos
            try {
                if (pst != null) {
                    pst.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();  // O loggear el error
            }
        }
    }

    public boolean RegistrarSalidaAparcamiento(String placa) {
        ConexionBaseDatos conexion = new ConexionBaseDatos();
        Connection connection = null;
        PreparedStatement pst = null;

        try {
            connection = conexion.conectar();

            VehiculoDto vehiculo = getVehiculo(placa);

            // Validamos si el vehículo se encuentra registrado
            if (vehiculo == null) {
                return false;
            }

            RegistroAparcamientoDto datosAparcamiento = getAparcamiento(placa);

            datosAparcamiento = calcularMontoTotal(datosAparcamiento);

            String sql = "UPDATE aparcamiento " +
                         "SET vehiculo_id=?, tiempo_total=?, monto_total=?, fecha_entrada=?, fecha_salida=?, activo=? " +
                         "WHERE id=?;";

            pst = connection.prepareStatement(sql);
            pst.setInt(1, vehiculo.getId());
            pst.setDouble(2, datosAparcamiento.aparcamientoDto().getTiempoTotal());
            pst.setDouble(3, datosAparcamiento.aparcamientoDto().getMontoTotal());
            pst.setTimestamp(4, Timestamp.valueOf(datosAparcamiento.aparcamientoDto().getFechaEntrada()));
            pst.setTimestamp(5, Timestamp.valueOf(datosAparcamiento.aparcamientoDto().getFechaSalida()));
            pst.setBoolean(6, false);
            pst.setInt(7, datosAparcamiento.aparcamientoDto().getId());

            int result = pst.executeUpdate();

            if (result != 0) {
                JOptionPane.showMessageDialog(null, "Vehículo con placa : " + placa +" \n"
                        + "Tiempo total : " + datosAparcamiento.aparcamientoDto().getTiempoTotal()+ " \n"
                        + "Valor a pagar : " + datosAparcamiento.aparcamientoDto().getMontoTotal() + " \n"
                );
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error registrando la salida del vehiculo : " + e);
            System.out.println(e);
            return false;
        } finally {
            // Asegurarse de cerrar los recursos
            try {
                if (pst != null) {
                    pst.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();  // O loggear el error
            }
        }
    }

    private RegistroAparcamientoDto getAparcamiento(String placa){
        ConexionBaseDatos conexion = new ConexionBaseDatos();
        Connection connection = conexion.conectar();
        try {
            // Usamos PreparedStatement para evitar SQL Injection
            String query = "SELECT a.*, t.monto, t.tiempo_hora FROM aparcamiento a " +
                    "INNER JOIN vehiculo v on a.vehiculo_id = v.id " +
                    "INNER JOIN tipo_vehiculo tv on v.tipo_vehiculo_id = tv.id " +
                    "INNER JOIN tarifa_tipo_vehiculo ttv on tv.id = ttv.tipo_vehiculo_id " +
                    "INNER JOIN tarifa t on t.id = ttv.tarifa_id " +
                    "WHERE v.placa LIKE ? AND a.activo = true ;";

            // Preparamos la consulta
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "%" + placa + "%");
            
            // Ejecutamos la consulta
            ResultSet result = preparedStatement.executeQuery();

            // Procesamos el resultado
            if (result.next()) {
                RegistroAparcamientoDto registroAparcamientoDto = new RegistroAparcamientoDto();
                registroAparcamientoDto.aparcamientoDto().setId(result.getInt("id"));
                registroAparcamientoDto.aparcamientoDto().setFechaEntrada(result.getTimestamp("fecha_entrada").toLocalDateTime());
                registroAparcamientoDto.setMonto(result.getDouble("monto"));
                registroAparcamientoDto.setTiempoHora(result.getInt("tiempo_hora"));
               return registroAparcamientoDto;
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Puedes gestionar el error según tu necesidad
            System.out.println("Error al traer el aparcamiento: "+ e.getMessage());
        } finally {
            try {
                // Cerramos la conexión (o puedes usar un pool de conexiones para optimizar)
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        JOptionPane.showMessageDialog(null, "Vehículo no encontrado.");
        return null;  // Si no se encuentra el vehículo
    }

    private RegistroAparcamientoDto calcularMontoTotal(RegistroAparcamientoDto datosAparcamiento){
        LocalDateTime fechaSalida = LocalDateTime.now();
        // Calcular la diferencia de tiempo en minutos entre fechaEntrada y fechaSalida
        long minutosTotales = java.time.Duration.between(datosAparcamiento.aparcamientoDto().getFechaEntrada(), fechaSalida).toMinutes();

        // Convertir minutos a horas fraccionadas (por ejemplo, 85 min = 1.416 horas)
        double tiempoTotal = minutosTotales / 60.0;

        // Aproximar el monto total al entero más cercano
        double montoTotal = Math.round(tiempoTotal * datosAparcamiento.monto());
        
        datosAparcamiento.aparcamientoDto().setMontoTotal(montoTotal);
        datosAparcamiento.aparcamientoDto().setTiempoTotal(tiempoTotal);
        datosAparcamiento.aparcamientoDto().setFechaSalida(fechaSalida);
        datosAparcamiento.aparcamientoDto().setActivo(false);
        
        return datosAparcamiento;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        registarAparacamiento = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        RegistarSalidaAparcamiento = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaListado = new javax.swing.JTable();
        BucarPlaca = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(831, 400));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Registro de Aparcamiento");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "Registrar Entrada", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Roboto", 1, 14))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N

        registarAparacamiento.setBackground(new java.awt.Color(0, 102, 51));
        registarAparacamiento.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        registarAparacamiento.setForeground(new java.awt.Color(255, 255, 255));
        registarAparacamiento.setText("Guardar");
        registarAparacamiento.setBorder(null);
        registarAparacamiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registarAparacamientoActionPerformed(evt);
            }
        });

        jTextField2.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        jTextField2.setToolTipText("");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        jLabel3.setText("Placa");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(registarAparacamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(registarAparacamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(41, 43, 45)), "Lista de Vehiculos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Roboto", 1, 14))); // NOI18N

        jTextField1.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        jTextField1.setToolTipText("");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        RegistarSalidaAparcamiento.setBackground(new java.awt.Color(255, 153, 153));
        RegistarSalidaAparcamiento.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        RegistarSalidaAparcamiento.setText("Dar Salida");
        RegistarSalidaAparcamiento.setBorder(null);
        RegistarSalidaAparcamiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegistarSalidaAparcamientoActionPerformed(evt);
            }
        });

        tablaListado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaListado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaListadoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tablaListado);

        BucarPlaca.setBackground(new java.awt.Color(204, 204, 255));
        BucarPlaca.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        BucarPlaca.setText("Buscar ");
        BucarPlaca.setBorder(null);
        BucarPlaca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BucarPlacaActionPerformed(evt);
            }
        });

        jLabel7.setText("Buscar Placa");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BucarPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(RegistarSalidaAparcamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(132, 132, 132))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1)
                    .addComponent(jLabel7)
                    .addComponent(BucarPlaca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RegistarSalidaAparcamiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(141, 141, 141))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        jPanel3.getAccessibleContext().setAccessibleName("historial de aparcamiento");
    }// </editor-fold>//GEN-END:initComponents

    private void registarAparacamientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registarAparacamientoActionPerformed

        if (jTextField2.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Debes ingresar la Placa del vehículo");
            jTextField2.requestFocus();
            return;
        }
       
        
        if (RegistrarEntradaAparcamiento(jTextField2.getText())) {
            JOptionPane.showMessageDialog(this, "Aparcamiento registado exitosamente");
            listarAparcamientos();
        }
    }//GEN-LAST:event_registarAparacamientoActionPerformed


    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void RegistarSalidaAparcamientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegistarSalidaAparcamientoActionPerformed
        if (jTextField1.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Debes ingresar la Placa del vehículo");
            jTextField1.requestFocus();
            return;
        }
       
        
        if (RegistrarSalidaAparcamiento(jTextField1.getText())) {
            JOptionPane.showMessageDialog(this, "Salida exitosa");
             listarAparcamientos();
        }
    }//GEN-LAST:event_RegistarSalidaAparcamientoActionPerformed

    private void tablaListadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaListadoMouseClicked

    }//GEN-LAST:event_tablaListadoMouseClicked

    
    private void listarAparcamientos(){
       try {
            DefaultTableModel modelo;
            modelo = obtenerDatosAparcamiento("");
            tablaListado.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error obteniendo datos: " + e);
        }
    }
    
    private void BucarPlacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BucarPlacaActionPerformed
        if (jTextField1.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Debes ingresar el valor de busqueda");
            jTextField1.requestFocus();
            return;
        }
        
        try {
            DefaultTableModel modelo;
            modelo = obtenerDatosAparcamiento(jTextField1.getText());
            tablaListado.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error obteniendo datos: " + e);
        }
    }//GEN-LAST:event_BucarPlacaActionPerformed

    private DefaultTableModel obtenerDatosAparcamiento(String placa){
        DefaultTableModel modelo;
        String[] titulos = {"nombre_propietario", "placa", "fecha_entrada", "activo"};
        String[] registro = new String[6];
        modelo = new DefaultTableModel(null, titulos);

        ConexionBaseDatos conexion = new ConexionBaseDatos();
        Connection connection = conexion.conectar();

        try {
            String query = "select v.nombre_propietario , v.placa, a.fecha_entrada , a.activo " +
                     "from aparcamiento a inner join vehiculo v on a.vehiculo_id = v.id " +
                     "where v.placa LIKE '%" + placa + "%' ";
        
            // Preparamos la consulta
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            
            // Ejecutamos la consulta
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                registro[0] = result.getString("nombre_propietario");
                registro[1] = result.getString("placa");
                registro[2] = result.getString("fecha_entrada");
                registro[3] = result.getString("activo").equals("1")? "Sí" : "No";

                modelo.addRow(registro);
            }
            return modelo;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BucarPlaca;
    private javax.swing.JButton RegistarSalidaAparcamiento;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JButton registarAparacamiento;
    private javax.swing.JTable tablaListado;
    // End of variables declaration//GEN-END:variables
}
