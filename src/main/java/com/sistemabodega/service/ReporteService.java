package com.sistemabodega.service;

// Permite leer el archivo JRXML desde los recursos del proyecto.
import java.io.InputStream;
// Permite trabajar con la conexión a la base de datos.
import java.sql.Connection;
// Permite almacenar los parámetros que recibirá JasperReports.
import java.util.HashMap;
import java.util.Map;

// Permite crear un archivo Excel en memoria antes de devolverlo.
import java.io.ByteArrayOutputStream;

// Permite obtener los archivos ubicados en src/main/resources.
import org.springframework.core.io.ClassPathResource;

// Indica a Spring que esta clase contiene lógica de servicio.
import org.springframework.stereotype.Service;

// Permite reutilizar la conexión configurada en Spring Boot.
import javax.sql.DataSource;

// Clases principales de JasperReports.
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

// Clases necesarias para exportar a Excel XLSX.
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

// genera los reportes y la boleta
@Service
public class ReporteService {

    // DataSource administra las conexiones MySQL configuradas
    // en application.properties.
    private final DataSource dataSource;

    // Constructor: Spring Boot inyecta automáticamente el DataSource.
    public ReporteService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
     * Método privado que prepara y ejecuta un reporte.
     *
     * nombreArchivo: nombre del archivo JRXML que se utilizará.
     * parametros: valores que recibe el reporte, por ejemplo,
     *             las fechas del periodo seleccionado.
     *
     * Devuelve un JasperPrint, que contiene el reporte procesado.
     */
    private JasperPrint generarReporte(String nombreArchivo, Map<String, Object> parametros) throws Exception {
        // Busca el archivo dentro de src/main/resources/reports.
        ClassPathResource recurso = new ClassPathResource("reports/" + nombreArchivo);
        // Abre el archivo JRXML y obtiene una conexión MySQL.
        try (InputStream archivo = recurso.getInputStream();
             Connection conexion = dataSource.getConnection()) {
            // Convierte el diseño JRXML en un reporte compilado.
            JasperReport reporte = JasperCompileManager.compileReport(archivo);
            // Ejecuta la consulta del JRXML con sus parámetros
            // y la conexión a MySQL.
            return JasperFillManager.fillReport(reporte, parametros, conexion);
        }
        // try-with-resources cierra el archivo y la conexión
        // automáticamente al terminar.
    }

    // Parametros de un reporte por periodo.
    // Las fechas llegan con formato yyyy-MM-dd desde el input type="date".
    private Map<String, Object> parametrosPeriodo(String fechaInicio, String fechaFin) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("fechaInicio", fechaInicio);
        parametros.put("fechaFin", fechaFin);
        return parametros;
    }

    // reporte de ventas por periodo

    public byte[] generarVentasPDF(String fechaInicio, String fechaFin) throws Exception {
        JasperPrint reporte = generarReporte("VentasPorPeriodo.jrxml", parametrosPeriodo(fechaInicio, fechaFin));
        return JasperExportManager.exportReportToPdf(reporte);
    }

    public byte[] generarVentasExcel(String fechaInicio, String fechaFin) throws Exception {
        JasperPrint reporte = generarReporte("VentasPorPeriodo.jrxml", parametrosPeriodo(fechaInicio, fechaFin));
        return exportarExcel(reporte);
    }

    // reporte de inventario

    public byte[] generarInventarioPDF() throws Exception {
        JasperPrint reporte = generarReporte("InventarioProductos.jrxml", new HashMap<>());
        return JasperExportManager.exportReportToPdf(reporte);
    }

    public byte[] generarInventarioExcel() throws Exception {
        JasperPrint reporte = generarReporte("InventarioProductos.jrxml", new HashMap<>());
        return exportarExcel(reporte);
    }

    // reporte de productos con stock bajo

    public byte[] generarStockBajoPDF() throws Exception {
        JasperPrint reporte = generarReporte("ProductosStockBajo.jrxml", new HashMap<>());
        return JasperExportManager.exportReportToPdf(reporte);
    }

    public byte[] generarStockBajoExcel() throws Exception {
        JasperPrint reporte = generarReporte("ProductosStockBajo.jrxml", new HashMap<>());
        return exportarExcel(reporte);
    }

    // reporte de movimientos de inventario por periodo

    public byte[] generarMovimientosPDF(String fechaInicio, String fechaFin) throws Exception {
        JasperPrint reporte = generarReporte("MovimientosInventario.jrxml", parametrosPeriodo(fechaInicio, fechaFin));
        return JasperExportManager.exportReportToPdf(reporte);
    }

    public byte[] generarMovimientosExcel(String fechaInicio, String fechaFin) throws Exception {
        JasperPrint reporte = generarReporte("MovimientosInventario.jrxml", parametrosPeriodo(fechaInicio, fechaFin));
        return exportarExcel(reporte);
    }

    // boleta de venta

    public byte[] generarBoletaPDF(Integer idVenta) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        // El nombre debe coincidir con el parámetro definido en Boleta.jrxml.
        parametros.put("idVenta", idVenta);
        JasperPrint reporte = generarReporte("Boleta.jrxml", parametros);
        return JasperExportManager.exportReportToPdf(reporte);
    }

    /* Convierte un JasperPrint en un archivo Excel XLSX.
     * Este método se reutiliza para todos los reportes.
     */
    private byte[] exportarExcel(JasperPrint reporte) throws Exception {
        // Crea una salida en memoria; no guarda un archivo
        // temporal en el disco del servidor.
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        // Crea el exportador XLSX de JasperReports.
        JRXlsxExporter exportador = new JRXlsxExporter();
        // Indica qué reporte se convertirá.
        exportador.setExporterInput(new SimpleExporterInput(reporte));
        // Indica dónde se escribirá el Excel generado.
        exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(salida));
        // Configura el formato de las celdas de Excel.
        SimpleXlsxReportConfiguration configuracion = new SimpleXlsxReportConfiguration();
        // Evita crear una hoja de Excel por cada página del reporte.
        configuracion.setOnePagePerSheet(false);
        // Intenta conservar los tipos de datos en las celdas.
        configuracion.setDetectCellType(true);
        // Elimina espacios vacíos innecesarios entre filas.
        configuracion.setRemoveEmptySpaceBetweenRows(true);
        // Aplica la configuración al exportador.
        exportador.setConfiguration(configuracion);
        // Ejecuta la exportación.
        exportador.exportReport();
        // Devuelve el contenido del Excel para que el controlador
        // pueda enviarlo al navegador como archivo descargable.
        return salida.toByteArray();
    }
}
