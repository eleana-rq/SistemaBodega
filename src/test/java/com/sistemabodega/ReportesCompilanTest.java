package com.sistemabodega;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import net.sf.jasperreports.engine.JasperCompileManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportesCompilanTest {

	@Test
	void todosLosReportesCompilan() throws Exception {
		Resource[] reportes = new PathMatchingResourcePatternResolver().getResources("classpath:reports/*.jrxml");
		assertTrue(reportes.length > 0, "No se encontraron reportes");
		for (Resource reporte : reportes) {
			try (InputStream archivo = reporte.getInputStream()) {
				JasperCompileManager.compileReport(archivo);
				System.out.println("Compilado: " + reporte.getFilename());
			}
		}
	}
}
