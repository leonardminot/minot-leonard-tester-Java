package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.FileConstant;
import com.parkit.parkingsystem.util.JsonReaderUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonReaderIT {
    @Test
    public void serverNameGetCheck() {
        // Given
        JsonReaderUtil jsonReaderUtil = new JsonReaderUtil();
        String JsonFile = FileConstant.DATABASE_CONNECTION_PROPERTIES;

        // When
        String user = jsonReaderUtil.getStringParameter(JsonFile, "user");
        String password = jsonReaderUtil.getStringParameter(JsonFile, "password");
        String url = jsonReaderUtil.getStringParameter(JsonFile, "url");

        // Then
        assertThat(user).isEqualTo("root");
        assertThat(password).isEqualTo("rootroot");
        assertThat(url).isEqualTo("jdbc:mysql://localhost:3306/prod");
    }
}
