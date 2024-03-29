package today.netshield.bukkit.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import today.netshield.bukkit.NetShield;

import java.io.IOException;
import java.util.List;

public class Authentication {
    public void handle(Player player) {
        OkHttpClient client = new OkHttpClient();

        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", NetShield.getInstance().getConfig().getString("KEY"));

        JsonObject playerData = new JsonObject();
        playerData.addProperty("name", player.getName());
        playerData.addProperty("ip", player.getAddress().getAddress().getHostAddress());

        jsonObject.add("playerData", playerData);

        String jsonString = gson.toJson(jsonObject);

        RequestBody requestBody = RequestBody.create(jsonString, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://netshield.bombardeen.me/api/checkuser")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                String code = jsonResponse.contains("\"code\"") ?
                        jsonResponse.split("\"code\"")[1].split(":")[1].split(",")[0].replaceAll("\"", "").trim() : null;

                if (code == null || !code.equalsIgnoreCase("VALID_PLAYER")) {
                    kickPlayer(player);
                }
            } else {
                CC.log("Error: " + response.code() + " - " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void kickPlayer(Player player) {
        List<String> kickMessage = NetShield.getInstance().getConfig().getStringList("KICK_MESSAGE");

        StringBuilder message = new StringBuilder();
        for (String line : kickMessage) {
            message.append(ChatColor.translateAlternateColorCodes('&', line)).append("\n");
        }
        Bukkit.getScheduler().runTask(NetShield.getInstance(), () -> {
            player.kickPlayer(message.toString());
        });
    }
}
