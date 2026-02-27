package com.itdontmatta.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.itdontmatta.omniscience.OmniConfig;
import com.itdontmatta.omniscience.api.interfaces.IOmniscience;
import com.itdontmatta.omniscience.api.util.Formatter;
import com.itdontmatta.omniscience.command.result.CommandResult;
import com.itdontmatta.omniscience.command.result.UseResult;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AICommand extends SimpleCommand {

    private static final long COOLDOWN_MS = 10_000; // 10 seconds between requests
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private String systemPrompt;

    public AICommand(JavaPlugin plugin) {
        super(ImmutableList.of("help-ai", "ask"));
        this.plugin = plugin;
        loadPrompt();
    }

    private void loadPrompt() {
        File promptFile = new File(plugin.getDataFolder(), "ai-prompt.txt");

        if (!promptFile.exists()) {
            plugin.saveResource("ai-prompt.txt", false);
        }

        try {
            systemPrompt = Files.readString(promptFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load ai-prompt.txt: " + e.getMessage());
            systemPrompt = "You are a helpful assistant for Omniscience, a Minecraft logging plugin. Help users construct search queries.";
        }
    }

    public void reloadPrompt() {
        loadPrompt();
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return hasPermission(sender, "omniscience.commands.ai");
    }

    @Override
    public String getCommand() {
        return "ai";
    }

    @Override
    public String getUsage() {
        return GREEN + "<question>";
    }

    @Override
    public String getDescription() {
        return "Ask AI for help constructing search queries";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        if (!OmniConfig.INSTANCE.isAIEnabled()) {
            return CommandResult.failure("AI features are not enabled. Set ai.enabled: true in config.yml");
        }

        String apiKey = OmniConfig.INSTANCE.getAIApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            return CommandResult.failure("No API key configured. Add your Vertex AI API key to config.yml");
        }

        if (args.length == 0) {
            return CommandResult.failure("Please provide a question. Example: /omni ai how do I find who broke blocks near me?");
        }

        // Rate limiting for players
        if (sender instanceof Player) {
            UUID playerId = ((Player) sender).getUniqueId();
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(playerId);

            if (lastUse != null && (now - lastUse) < COOLDOWN_MS) {
                long remaining = (COOLDOWN_MS - (now - lastUse)) / 1000;
                return CommandResult.failure("Please wait " + remaining + " seconds before asking another question.");
            }

            cooldowns.put(playerId, now);
        }

        String question = String.join(" ", args);
        sender.sendMessage(Formatter.formatPrimaryMessage("Thinking..."));

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String response = queryVertexAI(question);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(Formatter.formatSecondaryMessage("--- AI Response ---"));
                            sendFormattedResponse(sender, response);
                        }
                    }.runTask(plugin);
                } catch (Exception e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(Formatter.error("AI request failed: " + e.getMessage()));
                        }
                    }.runTask(plugin);
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);

        return CommandResult.success();
    }

    private String queryVertexAI(String question) throws Exception {
        String apiKey = OmniConfig.INSTANCE.getAIApiKey();
        String modelId = OmniConfig.INSTANCE.getAIModelId();

        // Vertex AI endpoint with API key
        String urlString = String.format(
                "https://aiplatform.googleapis.com/v1/publishers/google/models/%s:generateContent?key=%s",
                modelId, apiKey
        );

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        // Build request body
        JsonObject requestBody = new JsonObject();

        // System instruction
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt);
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        requestBody.add("system_instruction", systemInstruction);

        // User content
        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", question);
        userParts.add(userPart);
        userContent.add("parts", userParts);
        contents.add(userContent);
        requestBody.add("contents", contents);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("API error (" + responseCode + "): " + errorResponse);
        }

        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse response
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
        if (candidates != null && candidates.size() > 0) {
            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts != null && parts.size() > 0) {
                return parts.get(0).getAsJsonObject().get("text").getAsString();
            }
        }

        throw new Exception("No response from AI");
    }

    private static final Pattern COMMAND_PATTERN = Pattern.compile("`(/omni[^`]+)`|(/omni\\s+\\S+(?:\\s+\\S+)*)");

    private void sendFormattedResponse(CommandSender sender, String response) {
        // Strip markdown code blocks
        response = response.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "");

        // Strip bold/italic markdown
        response = response.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        response = response.replaceAll("\\*([^*]+)\\*", "$1");

        for (String line : response.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Check if line contains a command
            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (matcher.find()) {
                // Extract command (from either backtick group or plain group)
                String command = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                command = command.trim();

                if (sender instanceof Player) {
                    // Build clickable component
                    TextComponent prefix = new TextComponent(line.substring(0, matcher.start()));
                    prefix.setColor(net.md_5.bungee.api.ChatColor.GRAY);

                    TextComponent cmdComponent = new TextComponent(command);
                    cmdComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                    cmdComponent.setBold(true);
                    cmdComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
                    cmdComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(net.md_5.bungee.api.ChatColor.GREEN + "Click to copy command")));

                    // Get suffix after the matched command
                    String suffix = line.substring(matcher.end());
                    TextComponent suffixComponent = new TextComponent(suffix);
                    suffixComponent.setColor(net.md_5.bungee.api.ChatColor.GRAY);

                    ((Player) sender).spigot().sendMessage(prefix, cmdComponent, suffixComponent);
                } else {
                    sender.sendMessage(GRAY + line);
                }
            } else {
                sender.sendMessage(GRAY + line);
            }
        }
    }

    @Override
    public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {
        builder.then(RequiredArgumentBuilder.argument("question", StringArgumentType.greedyString()));
    }

    @Override
    public List<String> getCommandSuggestions(String partial) {
        return Collections.emptyList();
    }
}
