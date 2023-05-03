package back.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestDate {


    @Test
    void 테스트() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        String date = LocalDateTime.now().format(format);

        LocalDateTime time = LocalDateTime.parse(date, format);

        LocalDate date1 = LocalDate.from(time);

        System.out.println("parse = " + date1);
    }
}
