package examenfinal.parqueadero.dto;

public class VehiculoDto {
    private int id;
    private String tipo, placa, marca, modelo, propietario;

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }
   
    
}
