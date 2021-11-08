package agendarcita;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javax.swing.JOptionPane;
//LIBRERIAS PARA CONECTARSE A LA BASE DE DATOS 
import java.sql.Connection;//conexion a BD
import java.sql.DriverManager;//driver de conexion
import java.sql.PreparedStatement;
import java.sql.ResultSet;//resultado final de datos
import java.sql.ResultSetMetaData;//resultado de metadatos
import java.sql.SQLException;//Tratamiento de Errros de BD SQL
import java.sql.Statement;//Generador de sentencias SQL
import java.text.SimpleDateFormat;//Formatear datos
import java.time.DayOfWeek;
import java.time.format.FormatStyle;
import java.util.Date;//fecha de sistema
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.converter.LocalDateStringConverter;
import javax.swing.JTextField;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JasperViewer;

public class FXMLCitasController implements Initializable {
     //clases de conexion y sus objetos
    public Connection cn;
    public Statement stmt;
    public ResultSet rs;
        
    @FXML
	private Label lblcitaFecha;
    @FXML
	private Label lblHora;
    @FXML
	private DatePicker calendarioCita; 
    @FXML
	private Button btnAgregar;
    @FXML
	private Button btnMostrar;
    @FXML
	private Button btnSalir;
    @FXML
	private Button btnCrearPdf;
    @FXML
	private Button btnImprimir;
    @FXML
	private TextField txtaAgregarHora;
    @FXML
    private TableView<modeloTabla> tabla_consultar;
    
    @FXML
    private TableColumn<modeloTabla,String> columna_fecha;

    @FXML
    private TableColumn<modeloTabla,String> columna_hora;

    @FXML
    private TableColumn<modeloTabla,String> columna_numCita;

    ObservableList<modeloTabla> oblist = FXCollections.observableArrayList();
      
   public void conectarBase(){
    try{ //inicia try
            Class.forName("com.mysql.jdbc.Driver");
            cn=DriverManager.getConnection("jdbc:mysql://localhost/bdcita","root","hola123");
            stmt=cn.createStatement();//genera sentencias sobre objetos de base de datos
    }catch(SQLException ex){
            JOptionPane.showMessageDialog(null,"Error de base de datos 1: \n"+ ex);
    }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Error de base de datos 2:"+ e);
    }
    }//termina metodo conectar
   
   public void mandarRegistroBD(){
     // llamada o invocacion a metodo conectar 
     conectarBase();
     String alta="";//variable para almacenar los valores capturados y enviados a la base de datos  
     alta= "INSERT INTO tregistro(hora,fecha) VALUES (?,?)";
              
     try {
         PreparedStatement pst = cn.prepareStatement(alta);
         pst.setString(1,lblHora.getText());
         pst.setString(2,lblcitaFecha.getText());
         pst.execute();
         JOptionPane.showMessageDialog(null,"Fecha y hora agregada con exito");
     } catch (Exception e) {
         JOptionPane.showConfirmDialog(null,"Error de registro"+ e);
     }
   }
     public void buscarCita_AgregarBD(){
        try {
            conectarBase();//llamando funcion para conectar a base de datos 
            rs=stmt.executeQuery("select * from tregistro where fecha ='"+lblcitaFecha.getText()+"'");//buscar la fecha en BD
            rs=stmt.executeQuery("select * from tregistro where hora ='"+lblHora.getText()+"'");//buscar hora en BD
            if(rs.next() == true){
                JOptionPane.showMessageDialog(null,"La cita esta ocupada, selecciona otra hora"); 
            }else{
                mandarRegistroBD();
            } 
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error de BD Busqueda"+ ex);
        } 
   }
    @FXML
	public void agendar(){ 
                LocalDate hoy = LocalDate.now();
                LocalDate fechaHoy = calendarioCita.getValue();
                String formatoDeFecha = fechaHoy.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));//formato para poner el dia, mes y año 
                
                if(fechaHoy== null|| fechaHoy.isBefore(hoy)){ // condicion para no escoger las fechas anteriores al actual 
                    JOptionPane.showMessageDialog(null,"La fecha "+ formatoDeFecha + " ha caducado\n\n          seleccione otra"); 
                    lblcitaFecha.setText("fecha invalida"); //mostrar en el label fecha invalida
                    lblHora.setText(txtaAgregarHora.getText());//mostrar en el label la hora
                }else{
                    lblcitaFecha.setText(formatoDeFecha); // mostrar en el label la fecha validada
                    lblHora.setText(txtaAgregarHora.getText());// mostrar en el label la hora
                    buscarCita_AgregarBD();// mandamos a llamar la funcion buscar si existe la BD y subirlo a la BD
                }
	}  
    @FXML
	public void mostrarTabla(){   
        try {
            conectarBase();
            ResultSet rs =cn.createStatement().executeQuery("select * from tregistro");
            while(rs.next()){
                oblist.add(new modeloTabla(rs.getString("numCita"),rs.getString("fecha"),rs.getString("hora")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(FXMLCitasController.class.getName()).log(Level.SEVERE, null, ex);
        }
        columna_numCita.setCellValueFactory(new PropertyValueFactory<>("numCita"));
        columna_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columna_hora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        
        tabla_consultar.setItems(oblist);         
        }
     @FXML
	public void crearPdf(){
             conectarBase();
        try {
            String rutaReporte="src/reportes/rptRegistrosDeCitas.jasper";
            JasperPrint rptlibrosPDF = JasperFillManager.fillReport(rutaReporte,null,cn);
            JasperViewer ventanaVisor = new JasperViewer(rptlibrosPDF,false);
            ventanaVisor.setTitle("Reporte de Citas");
            ventanaVisor.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Error de BD en informe Verifica\n\n"+e);
        }
        
        }
        
      @FXML
	public void imprimirReportePdf(){       
          conectarBase();
          try {
            String rutaReporte="src/reportes/rptRegistrosDeCitas.jasper";
            JasperPrint rptlibrosPDF = JasperFillManager.fillReport(rutaReporte,null,cn);
            JasperPrintManager.printReport(rptlibrosPDF,true);
            JOptionPane.showMessageDialog(null,"Enviando reporte a impresora...");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Error de BD en informe Verifica\n\n"+e);
        }  
        }
        
     @FXML
	public void salir(){
            
        int confirmaSalida=JOptionPane.showConfirmDialog(null,"¿Quieres salir?","Mensaje importante",JOptionPane.YES_NO_OPTION);
        if (confirmaSalida==0) {
            System.exit(0);//cierra ventana     
            } 
	}      
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
    }     
}
