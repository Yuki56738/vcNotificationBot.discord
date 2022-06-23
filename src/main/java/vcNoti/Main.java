package vcNoti;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.*;

import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readString;

public class Main {
    //    public static HashMap<String, String> settings = new HashMap<String, String>();
//    public static Map<String, String > settingsGlobal = new HashMap<>();
    public static void main(String[] args) throws IOException {
//        System.out.println("Hello world!");
        String TOKEN = null;
        Dotenv dotenv = null;
        if (Files.exists(Paths.get(".env"))) {
            dotenv = Dotenv.load();
            TOKEN = dotenv.get("DISCORD_TOKEN");
            System.out.println("discord token read with dotenv-java.");
        } else {
            try {
                TOKEN = readString(Paths.get("token.txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        DiscordApi api = new DiscordApiBuilder()
                .setToken(TOKEN)
                .login().join();
        System.out.println("discord bot built.");

        //read settings.json as file
//        Path settingsFilePath = Paths.get("settings.json");
//        File settingsFile = settingsFilePath.toFile();
        System.out.println("Reading servers.json...");
        Gson gson = new Gson();
//        ServerData serverData1 = null;
        Type listType = new TypeToken<HashMap<String, String>>() {
        }.getType();

        File serversFile = new File("servers.json");
        if (!serversFile.exists()) {
//            serversFile.createNewFile();
            HashMap<String, String> map = new HashMap<>();
            System.out.println(gson.toJson(map));
            FileWriter writer = new FileWriter(serversFile);
            writer.write(gson.toJson(map));
            writer.close();
        }
//        HashMap<String, String> settings = new HashMap<>();
        HashMap<String, String> servers = gson.fromJson(Files.readString(Paths.get("servers.json")), listType);
//        JsonObject jsonObject = gson.fromJson(Files.readString(Paths.get("settings.json")), JsonObject.class);
        System.out.println(gson.toJson(servers));

//        HashMap<String, String> map = new HashMap<>();
//        map.put("key", "value");
//        JSONObject jsonObject = new JSONObject(map);
//        System.out.println(jsonObject);

//        JsonObject jsonObject = gson.fromJson(Paths.get("./settings.json"), listType);
//        Path jsonfile = Paths.get("settings.json");
//        String json = Files.readString(jsonfile);
//        JsonObject jsonObject1 = gson.fromJson(json, listType);
//        System.out.println(jsonObject1);

//        try {
//            FileReader fileReader1 = new FileReader(settingsFile);
////            serverData1 = gson.fromJson(fileReader1, ServerData.class);
////            JsonReader jsonReader = new JsonReader(fileReader1);
////            System.out.println(jsonReader.);
//
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(serverData1.getServerName());


        SlashCommand commandSet = SlashCommand.with("regtext", "register notification text channel")
                .createForServer(api.getServerById("880524381065986078").get()).join();
        SlashCommand command2 = SlashCommand.with("regtext", "register notification text channel.")
                .createForServer(api.getServerById("839941661898702899").get()).join();
        SlashCommand command3 = SlashCommand.with("regtext", "register notification text channel.")
                .createForServer(api.getServerById("977138017095520256").get()).join();
        SlashCommand slashCommandSet2 = SlashCommand.with("regtext", "今いるテキストチャンネルを通知用として設定。")
                .createGlobal(api)
                .join();
//        ServerData finalServerData = new ServerData();
//        ServerData finalServerData1 = serverData1;
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (slashCommandInteraction.getCommandName().equalsIgnoreCase("regtext")) {
                slashCommandInteraction.createImmediateResponder().setContent("Getting text channel...").respond().join();
//                TextChannel textChannel = slashCommandInteraction.getChannel().get();
                ServerTextChannel serverTextChannel = (ServerTextChannel) slashCommandInteraction.getChannel().get();
                Server server = slashCommandInteraction.getServer().get();
//                jsonObject.addProperty(server.getIdAsString(), serverTextChannel.getIdAsString());
//                System.out.println(jsonObject.toString());
                servers.put(server.getIdAsString(), serverTextChannel.getIdAsString());
                JSONObject jsonObject = new JSONObject(servers);
                System.out.println(jsonObject);
                System.out.println("Writing servers.json...");
                File file = new File("servers.json");
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObject.toString());
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }


        });

//        TextChannel textChannelForNotification = api.getTextChannelById("896372417977528361").get();
        api.addServerVoiceChannelMemberJoinListener(event -> {
            if (event.getOldChannel().isEmpty()) {
                System.out.println("ServerVoiceChannelMemberJoinListener hit.");
                if (servers.get(event.getServer().getIdAsString()) == null) {
                    System.out.println(String.format("Server %s is not in servers.json. Please add with slash command /regtext .", event.getServer().getName()));
                } else {
                    System.out.println(String.format("in ServerVoiceChannelMemberJoinListener: Sending message..."));
                    ServerTextChannel serverTextChannel = api.getServerTextChannelById(servers.get(event.getServer().getIdAsString())).get();
                    serverTextChannel.sendMessage(String.format("%sが%sに入ったよ〜。", event.getUser().getDisplayName(event.getServer()).toString(), event.getChannel().getName().toString()));
                }

            }
        });
        api.addServerVoiceChannelMemberLeaveListener(event -> {
            if (event.getNewChannel().isEmpty()) {
                System.out.println("left listener hit.");
                if (servers.get(event.getServer().getIdAsString()) == null) {
                    System.out.println(String.format("Server %s is not in servers.json. Please add with slash command /regtext .", event.getServer().getName()));
                } else {
                    System.out.println(String.format("in ServerVoiceChannelMemberJoinListener: Sending message..."));
                    ServerTextChannel serverTextChannel = api.getServerTextChannelById(servers.get(event.getServer().getIdAsString())).get();
                    serverTextChannel.sendMessage(String.format("%sが%sから出たよ〜。", event.getUser().getDisplayName(event.getServer()), event.getChannel().getName().toString()));
                }
            }
        });


    }

}
