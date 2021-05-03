package dev.jeschke.diswork;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Stream;

import static dev.jeschke.diswork.Main.DISCORD_CHANNEL;

public class DailyMeetingJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(DailyMeetingJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.debug("execute(): Executing Job");
        final GatewayDiscordClient gateway = (GatewayDiscordClient) jobExecutionContext.getJobDetail().getJobDataMap().get(Main.KEY_GATEWAY);
        final MessageChannel channel = gateway.getChannelById(Snowflake.of(DISCORD_CHANNEL))
                .map(MessageChannel.class::cast)
                .block();
        if (channel == null) {
            logger.error("execute(): Couldn't access channel");
            throw new JobExecutionException("Couldn't access channel");
        }
        logger.debug("execute(): Sending message");
        final LocalDateTime now = LocalDateTime.now();
        channel.createMessage(getMessage()).block());
    }
    private static String getMessage() {
        return "**Daily!**\nHeute ist " + now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", der " + now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) +
        (Stream.of(Main.PRESENCE_DAYS.split(";")).map(DayOfWeek::valueOf).anyMatch(d -> d == now.getDayOfWeek())? ("\n" + Main.PRESENCE_NAME + " ist heute da!"):"");
    }
}
