package report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Component("JasperReportGenerator")
public class JasperReportGenerator {

    private static final String REPORT_TEMPLATE_PATH = "/reportTemplate.jrxml";

    @Cacheable("report")
    public byte[] generate(WeatherStation weatherStation, String cityName, LocalDate date) {
        try {
            System.out.println("Generating report: " + date + ", " + cityName);
            return tryGenerateReport(weatherStation, cityName, date);
        } catch (JRException e) {
            throw new ReportGenerationException(e);
        }
    }

    private byte[] tryGenerateReport(WeatherStation weatherStation, String cityName, LocalDate date) throws JRException {
        List<WeatherData> weatherData = weatherStation.getMeasurementsFor(cityName, date);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(weatherData);
        JasperDesign jasperDesign = JRXmlLoader.load(getClass().getResourceAsStream(REPORT_TEMPLATE_PATH));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        return fillAndExportReport(dataSource, jasperReport);
    }

    private byte[] fillAndExportReport(JRBeanCollectionDataSource dataSource, JasperReport jasperReport) throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), dataSource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        return outputStream.toByteArray();
    }

    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(JRException cause) {
            super(cause);
        }
    }
}
