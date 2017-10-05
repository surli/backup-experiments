package com.cronutils.model.time.generator;

import com.google.common.base.Optional;
import org.junit.Test;
import org.threeten.bp.*;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.util.*;

import static org.junit.Assert.*;

public class ExecutionTimeUnixIntegrationTest {

    @Test
    public void testIsMatchForUnix01(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "* * * * *";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    @Test
    public void testIsMatchForUnix02(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "0 * * * 1-5";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-04T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    /**
     * Issue #37: for pattern "every 10 minutes", nextExecution returns a date from past.
     */
    @Test
    public void testEveryTenMinutesNextExecution(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("*/10 * * * *"));
        ZonedDateTime time = ZonedDateTime.parse("2015-09-05T13:43:00.000-07:00");
        assertEquals(ZonedDateTime.parse("2015-09-05T13:50:00.000-07:00"), executionTime.nextExecution(time).get());
    }

    /**
     * Issue #38: every 2 min schedule doesn't roll over to next hour
     */
    @Test
    public void testEveryTwoMinRollsOverHour(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        Cron cron = new CronParser(cronDefinition).parse("*/2 * * * *");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime time = ZonedDateTime.parse("2015-09-05T13:56:00.000-07:00");
        ZonedDateTime next = executionTime.nextExecution(time).get();
        ZonedDateTime shouldBeInNextHour = executionTime.nextExecution(next).get();
        assertEquals(next.plusMinutes(2), shouldBeInNextHour);
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayNextExecution(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron myCron = parser.parse("0 3 * * 3");
        ZonedDateTime time = ZonedDateTime.parse("2015-09-17T00:00:00.000-07:00");
        assertEquals(ZonedDateTime.parse("2015-09-23T03:00:00.000-07:00"), ExecutionTime.forCron(myCron).nextExecution(time).get());
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayLastExecution(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron myCron = parser.parse("0 3 * * 3");
        ZonedDateTime time = ZonedDateTime.parse("2015-09-17T00:00:00.000-07:00");
        assertEquals(ZonedDateTime.parse("2015-09-16T03:00:00.000-07:00"), ExecutionTime.forCron(myCron).lastExecution(time).get());
    }

    /**
     * Issue #45: last execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayLastExecution(){
        String crontab = "* * * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2015-10-13T17:26:54.468-07:00");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2015-10-12T23:59:00.000-07:00"), executionTime.lastExecution(date).get());
    }

    /**
     * Issue #45: next execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayNextExecution(){
        String crontab = "* * * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2015-10-13T17:26:54.468-07:00");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2015-10-19T00:00:00.000-07:00"), executionTime.nextExecution(date).get());
    }

    /**
     * Issue #50: last execution does not match expected date when cron specifies day of week and last execution is in previous month.
     */
    @Test
    public void testLastExecutionDaysOfWeekOverMonthBoundary(){
        String crontab = "0 11 * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2015-11-02T00:10:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2015-10-26T11:00:00Z"), executionTime.lastExecution(date).get());
    }

    /**
      * Issue #52: "And" doesn't work for day of the week
      * 1,2 should be Monday and Tuesday, but instead it is treated as 1st/2nd of month.
      */
    @Test
    public void testWeekdayAndLastExecution() {
        String crontab = "* * * * 1,2";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2015-11-10T17:01:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2015-11-10T17:00:00Z"), executionTime.lastExecution(date).get());
    }

    /**
     * Isue #52: Additional test to ensure after fix that "And" and "Between" can both be used
     * 1,2-3 should be Monday, Tuesday and Wednesday.
     */
    @Test
    public void testWeekdayAndWithMixOfOnAndBetweenLastExecution() {
        String crontab = "* * * * 1,2-3";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2015-11-10T17:01:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2015-11-10T17:00:00Z"), executionTime.lastExecution(date).get());
    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week"
     * Considers Month in range 0-11 instead of 1-12
     */
    @Test
    public void testCorrectMonthScaleForNextExecution1(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * */3 */4 */5";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime scanTime = ZonedDateTime.parse("2015-12-10T16:32:56.586-08:00");
        ZonedDateTime nextExecutionTime = executionTime.nextExecution(scanTime).get();
        //DoW: 0-6 -> 0, 5 (sunday, friday)
        //DoM: 1-31 -> 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31
        //M: 1-12 -> 1, 5, 9
        assertEquals(ZonedDateTime.parse("2016-01-01T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #59: Incorrect next execution time for "day of month" in "time" situation
     * dom "* / 4" should mean 1, 5, 9, 13, 17th... of month instead of 4, 8, 12, 16th...
     */
    @Test
    public void testCorrectMonthScaleForNextExecution2(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * */4 * *";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime scanTime = ZonedDateTime.parse("2015-12-10T16:32:56.586-08:00");
        ZonedDateTime nextExecutionTime = executionTime.nextExecution(scanTime).get();
        assertEquals(ZonedDateTime.parse("2015-12-13T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week"
     * Considers bad DoW
     */
    @Test
    public void testCorrectNextExecutionDoW(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * * * */4";//m,h,dom,M,dow
        //DoW: 0-6 -> 0, 4 (sunday, thursday)
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-01-28T16:32:56.586-08:00");
        ZonedDateTime nextExecutionTime = executionTime.nextExecution(scanTime).get();
        assertEquals(ZonedDateTime.parse("2016-02-04T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #69: Getting next execution fails on leap-year when using day-of-week
     */
    @Test
    public void testCorrectNextExecutionDoWForLeapYear(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "0 * * * 1-5";//m,h,dom,M,dow
        //DoW: 0-6 -> 1, 2, 3, 4, 5 -> in this year:
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(crontab));
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00");
        ZonedDateTime nextExecutionTime = executionTime.nextExecution(scanTime).get();
        assertEquals(ZonedDateTime.parse("2016-02-29T12:00:00.000-06:00"), nextExecutionTime);
    }

    /**
     * Issue #61: nextExecution over daylight savings is wrong
     */
    @Test
    public void testNextExecutionDaylightSaving() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        ZonedDateTime last = ZonedDateTime.of(2016, 3, 12, 17, 0, 0, 0, ZoneId.of("America/New_York"));
        ZonedDateTime next = executionTime.nextExecution(last).get();
        long millis = Duration.between(last, next).toMillis();
        assertEquals(23, (millis / 3600000));
        assertEquals(last.getZone(), next.getZone());
    }

    /**
     * Issue #61: lastExecution over daylight savings is wrong
     */
    @Test
    public void testLastExecutionDaylightSaving(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        ZonedDateTime now = ZonedDateTime.of(2016, 3, 12, 17, 0, 0, 0, ZoneId.of("America/Phoenix"));
        ZonedDateTime last = executionTime.lastExecution(now).get();
        long millis = Duration.between(last, now).toMillis();
        assertEquals(24, (millis / 3600000));
        assertEquals(now.getZone(), last.getZone());
    }

    /**
     * Issue #79: Next execution skipping valid date
     */
    @Test
    public void testNextExecution2014() {
        String crontab = "0 8 * * 1";//m,h,dom,m,dow ; every monday at 8AM
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        ZonedDateTime date = ZonedDateTime.parse("2014-11-30T00:00:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(ZonedDateTime.parse("2014-12-01T08:00:00Z"), executionTime.nextExecution(date).get());
    }

    /**
     * Issue #92: Next execution skipping valid date
     */
    @Test
    public void testNextExecution2016() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("1 0 * * tue"));
        ZonedDateTime date = ZonedDateTime.parse("2016-05-24T01:02:50Z");
        assertEquals(ZonedDateTime.parse("2016-05-31T00:01:00Z"), executionTime.nextExecution(date).get());
    }

    /**
     * Issue #112: Calling nextExecution exactly on the first instant of the fallback hour (after the DST ends) makes it go back to DST.
     * https://github.com/jmrozanec/cron-utils/issues/112
     */
    @Test
    public void testWrongNextExecutionOnDSTEnd() throws Exception {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");

        //2016-02-20T23:00-03:00[America/Sao_Paulo], first minute of fallback hour
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1456020000000L), zone);
        ZonedDateTime expected = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1456020000000L + 60000), zone);

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* * * * *"));
        assertEquals(expected, executionTime.nextExecution(date).get());
    }

    /**
     * Issue #112: Calling nextExecution on a date-time during the overlap hour of DST causes an incorrect offset
     * to be returned
     */
    @Test
    public void testDSTOverlap() throws Exception {
        ZoneId zoneId = ZoneId.of("America/New_York");

        // For the America/New_York time zone, DST ends (UTC-4:00 to UTC-5:00 / EDT -> EST) at 2:00 AM
        // on the these days for the years 2015-2026:
        Set<LocalDate> dstDates = new HashSet<>();
        dstDates.add(LocalDate.of(2015, Month.NOVEMBER, 1));
        dstDates.add(LocalDate.of(2016, Month.NOVEMBER, 6));
        dstDates.add(LocalDate.of(2017, Month.NOVEMBER, 5));
        dstDates.add(LocalDate.of(2018, Month.NOVEMBER, 4));
        dstDates.add(LocalDate.of(2019, Month.NOVEMBER, 3));
        dstDates.add(LocalDate.of(2020, Month.NOVEMBER, 1));
        dstDates.add(LocalDate.of(2021, Month.NOVEMBER, 7));
        dstDates.add(LocalDate.of(2022, Month.NOVEMBER, 6));
        dstDates.add(LocalDate.of(2023, Month.NOVEMBER, 5));
        dstDates.add(LocalDate.of(2024, Month.NOVEMBER, 3));
        dstDates.add(LocalDate.of(2025, Month.NOVEMBER, 2));
        dstDates.add(LocalDate.of(2026, Month.NOVEMBER, 1));

        // Starting at 12 AM Nov. 1, 2015
        ZonedDateTime date = ZonedDateTime.of(2015, 11, 1, 0, 0, 0, 0, zoneId);

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        // Scheduling pattern for 1:30 AM for the first 7 days of every November
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("30 1 1-7 11 *"));


        final ZoneOffset EDT = ZoneOffset.ofHours(-4);
        final ZoneOffset EST = ZoneOffset.ofHours(-5);

        for(int year = 2015; year <= 2026; year++){
            boolean pastDSTEnd = false;
            int dayOfMonth = 1;
            while(dayOfMonth < 8){
                LocalDateTime expectedLocalDateTime = LocalDateTime.of(year, Month.NOVEMBER, dayOfMonth, 1, 30);
                Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
                assert(nextExecution.isPresent());
                date = nextExecution.get();

                ZoneOffset expectedOffset = pastDSTEnd ? EST : EDT;

                if (dstDates.contains(LocalDate.of(year, Month.NOVEMBER, dayOfMonth))){
                    if (!pastDSTEnd){
                        // next iteration should be past the DST transition
                        pastDSTEnd = true;
                    }
                    else {
                        dayOfMonth++;
                    }
                }
                else {
                    dayOfMonth++;
                }
                assertEquals(ZonedDateTime.ofInstant(expectedLocalDateTime, expectedOffset, zoneId), date);
            }
        }
    }

    /**
     * Test that a cron expression that only runs at a certain time that falls inside the DST start gap
     * does not run on the DST start day. Ex. 2:15 AM is an invalid local time for the America/New_York
     * time zone on the DST start days.
     */
    @Test
    public void testDSTGap() throws Exception{
        ZoneId zoneId = ZoneId.of("America/New_York");
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        // Run at 2:15 AM each day for March 7 to 14
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("15 2 7-14 3 *"));

        // Starting at 12 AM March. 7, 2015
        ZonedDateTime date = ZonedDateTime.of(2015, 3, 7, 0, 0, 0, 0, zoneId);

        // For America/New_York timezone, DST starts at 2 AM local time and moves forward 1 hour
        // DST dates for 2015-2026
        Map<Integer, LocalDate> dstDates = new HashMap<>();
        dstDates.put(2015, LocalDate.of(2015, Month.MARCH, 8));
        dstDates.put(2016, LocalDate.of(2016, Month.MARCH, 13));
        dstDates.put(2017, LocalDate.of(2017, Month.MARCH, 12));
        dstDates.put(2018, LocalDate.of(2018, Month.MARCH, 11));
        dstDates.put(2019, LocalDate.of(2019, Month.MARCH, 10));
        dstDates.put(2020, LocalDate.of(2020, Month.MARCH, 8));
        dstDates.put(2021, LocalDate.of(2021, Month.MARCH, 14));
        dstDates.put(2022, LocalDate.of(2022, Month.MARCH, 13));
        dstDates.put(2023, LocalDate.of(2023, Month.MARCH, 12));
        dstDates.put(2024, LocalDate.of(2024, Month.MARCH, 10));
        dstDates.put(2025, LocalDate.of(2025, Month.MARCH, 9));
        dstDates.put(2026, LocalDate.of(2026, Month.MARCH, 8));


        final ZoneOffset EDT = ZoneOffset.ofHours(-4);
        final ZoneOffset EST = ZoneOffset.ofHours(-5);
        for(int year = 2015; year <= 2026; year++){
            LocalDate dstDateForYear = dstDates.get(year);
            boolean isPastDSTStart = false;
            int dayOfMonth = 7;
            while(dayOfMonth < 15) {
                LocalDateTime localDateTime = LocalDateTime.of(year, Month.MARCH, dayOfMonth, 2, 15);
                // skip the DST start days... 2:15 AM does not exist in the local time
                if (localDateTime.toLocalDate().isEqual(dstDateForYear)){
                    dayOfMonth++;
                    isPastDSTStart = true;
                    continue;
                }
                ZoneOffset expectedOffset = isPastDSTStart ? EDT : EST;
                ZonedDateTime expectedDateTime = ZonedDateTime.ofLocal(localDateTime, zoneId, expectedOffset);

                Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
                assert (nextExecution.isPresent());
                date = nextExecution.get();
                assertEquals(expectedDateTime, date);
                dayOfMonth++;
            }
        }
    }

    /**
     * Issue #125: Prints stack trace for NoSuchValueException for expressions with comma-separated values
     * https://github.com/jmrozanec/cron-utils/issues/125
     */
    @Test
    public void testNextExecutionProducesStackTraces() throws Exception {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("45 1,13 * * *"));
        executionTime.nextExecution(ZonedDateTime.parse("2016-05-24T01:02:50Z"));
    }

    /**
     * Issue #130: Wrong last execution time if schedule hit is less than one second ago
     * https://github.com/jmrozanec/cron-utils/issues/130
     */
    @Test
    public void exact_hit_returns_full_interval_duration() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse("0 12 * * *");
        final ZonedDateTime time = ZonedDateTime.of(2016, 12, 2, 12, 0, 0, 0, ZoneId.of("Europe/Vienna"));
        final Duration timeFromLastExecution = ExecutionTime.forCron(cron).timeFromLastExecution(time).get();
        assertEquals(timeFromLastExecution, Duration.ofHours(24));
    }

    /**
     * Issue #130: Wrong last execution time if schedule hit is less than one second ago
     * https://github.com/jmrozanec/cron-utils/issues/130
     */
    @Test
    public void fuzzy_hit_returns_very_small_interval_duration() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse("0 12 * * *");
        ZonedDateTime time = ZonedDateTime.of(2016, 12, 2, 12, 0, 0, 0, ZoneId.of("Europe/Vienna"));
        Duration diff = Duration.ofMillis(300);
        time = time.plus(diff);
        final Duration timeFromLastExecution = ExecutionTime.forCron(cron).timeFromLastExecution(time).get();
        assertEquals(timeFromLastExecution, Duration.ofDays(1).plus(diff));
    }
}
