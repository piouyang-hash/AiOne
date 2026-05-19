package org.myfx.controls.aione;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {
        "org.myfx.controls.aione.UserService.mapper", // Jar包的Mapper路径，必须加！
        "org.myfx.controls.aione.ServiceCommon.mapper",
        "org.myfx.controls.aione.AiService.mapper",
        "org.myfx.controls.aione.SimulationGame.mapper"
})
public class AiOneApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOneApplication.class, args);
    }

}
