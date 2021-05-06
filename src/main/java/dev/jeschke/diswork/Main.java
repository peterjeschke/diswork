package dev.jeschke.diswork;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.entity.RestChannel;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final String DISCORD_TOKEN = System.getenv("DISCORD_TOKEN");
    static final String DISCORD_CHANNEL = System.getenv("DISCORD_CHANNEL");
    static final String DAILY_CRON = System.getenv("DAILY_CRON");
    static final String PRESENCE_NAME = System.getenv("PRESENCE_NAME");
    static final String PRESENCE_DAYS = System.getenv("PRESENCE_DAYS");

    static final String KEY_GATEWAY = "gateway";

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SchedulerException {
        final DiscordClient client = DiscordClient.create(DISCORD_TOKEN);
        final GatewayDiscordClient gateway = client.login().block();
        if (gateway == null) {
            logger.error("main(): Couldn't obtain a gateway");
            throw new NullPointerException("Couldn't obtain a gateway.");
        }

        if (args.length > 1 && args[0].equals("send")) {
            final MessageChannel channel = gateway.getChannelById(Snowflake.of(DISCORD_CHANNEL))
                    .map(MessageChannel.class::cast)
                    .block();
            if (channel == null) {
                logger.error("execute(): Couldn't access channel");
                throw new JobExecutionException("Couldn't access channel");
            }
            channel.createMessage(args[1]).block();
            return;
        }

        final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        logger.debug("main(): Starting scheduler");
        scheduler.start();
        scheduler.getContext().put(KEY_GATEWAY, gateway);
        final JobDetail job = JobBuilder.newJob(DailyMeetingJob.class)
                .build();
        job.getJobDataMap().put(KEY_GATEWAY, gateway);
        logger.debug("main(): Scheduling job with cron {}", DAILY_CRON);
        scheduler.scheduleJob(
                job,
                TriggerBuilder.newTrigger()
                        .startNow()
                        .withSchedule(CronScheduleBuilder.cronSchedule(DAILY_CRON))
                        .build());
    }
}
