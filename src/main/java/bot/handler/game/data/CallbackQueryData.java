package bot.handler.game.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallbackQueryData {

    private int phase;
    private String data;

    @SneakyThrows(IOException.class)
    public static CallbackQueryData parseQueryData(String data) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, CallbackQueryData.class);
    }

    @SneakyThrows(IOException.class)
    public String writeToJson() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
