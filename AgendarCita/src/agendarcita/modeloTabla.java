package agendarcita;
public class modeloTabla {
    String numCita,fecha,hora;

    public modeloTabla(String numCita, String fecha, String hora) {
        this.numCita = numCita;
        this.fecha = fecha;
        this.hora = hora;
    }
    public String getNumCita() {
        return numCita;
    }

    public void setNumCita(String numCita) {
        this.numCita = numCita;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }    
}
