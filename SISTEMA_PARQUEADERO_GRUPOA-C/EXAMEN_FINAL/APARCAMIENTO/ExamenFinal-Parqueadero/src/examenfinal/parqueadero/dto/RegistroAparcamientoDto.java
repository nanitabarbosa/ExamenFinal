package examenfinal.parqueadero.dto;

public class RegistroAparcamientoDto {

    private AparcamientoDto aparcamientoDto;

    private Double monto;
    private int tiempoHora;

    public RegistroAparcamientoDto() {
        aparcamientoDto = new AparcamientoDto();
    }

    public AparcamientoDto aparcamientoDto() {
        return aparcamientoDto;
    }

    public RegistroAparcamientoDto setAparcamientoDto(AparcamientoDto aparcamientoDto) {
        this.aparcamientoDto = aparcamientoDto;
        return this;
    }

    public Double monto() {
        return monto;
    }

    public RegistroAparcamientoDto setMonto(Double monto) {
        this.monto = monto;
        return this;
    }

    public int tiempoHora() {
        return tiempoHora;
    }

    public RegistroAparcamientoDto setTiempoHora(int tiempoHora) {
        this.tiempoHora = tiempoHora;
        return this;
    }
}
