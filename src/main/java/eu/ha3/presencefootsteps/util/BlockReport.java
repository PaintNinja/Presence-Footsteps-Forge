package eu.ha3.presencefootsteps.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.SoundType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.google.gson.stream.JsonWriter;
import net.neoforged.fml.loading.FMLPaths;

public interface BlockReport {
    static CompletableFuture<?> execute(Reportable reportable, String baseName, boolean full) {
        Minecraft client = Minecraft.getInstance();
        ChatComponent hud = client.gui.getChat();
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path loc = getUniqueFileName(FMLPaths.GAMEDIR.get().resolve("presencefootsteps"), baseName, ".json");
                Files.createDirectories(loc.getParent());
                try (var writer = JsonObjectWriter.of(new JsonWriter(Files.newBufferedWriter(loc)))) {
                    reportable.writeToReport(full, writer, new Object2ObjectOpenHashMap<>());
                }
                return loc;
            } catch (IOException e) {
                throw new RuntimeException("Could not generate report", e);
            }
        }, Util.ioPool()).thenAcceptAsync(loc -> {
            hud.addMessage(Component.translatable("pf.report.save", Component.literal(loc.getFileName().toString()).withStyle(s -> s
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, loc.toString()))
                    .applyFormat(ChatFormatting.UNDERLINE)))
                .withStyle(s -> s
                    .withColor(ChatFormatting.GREEN)));
        }, client).exceptionallyAsync(e -> {
            hud.addMessage(Component.translatable("pf.report.error", e.getMessage()).withStyle(s -> s.withColor(ChatFormatting.RED)));
            return null;
        }, client);
    }

    private static Path getUniqueFileName(Path directory, String baseName, String ext) {
        Path loc = null;

        int counter = 0;
        while (loc == null || Files.exists(loc)) {
            loc = directory.resolve(baseName + (counter == 0 ? "" : "_" + counter) + ext);
            counter++;
        }

        return loc;
    }

    interface Reportable {
        void writeToReport(boolean full, JsonObjectWriter writer, Map<String, SoundType> groups) throws IOException;
    }
}
