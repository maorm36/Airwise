export class ScheduleConstants {
    static DAYS = {
        MONDAY: 'MONDAY',
        TUESDAY: 'TUESDAY',
        WEDNESDAY: 'WEDNESDAY',
        THURSDAY: 'THURSDAY',
        FRIDAY: 'FRIDAY',
        SATURDAY: 'SATURDAY',
        SUNDAY: 'SUNDAY'
    };

    static ACTIONS = {
        TURN_ON: 'TURN_ON',
        TURN_OFF: 'TURN_OFF',
    };

    static prefModes = {
        CURRENT: 'CURRENT',
        CUSTOM: 'CUSTOM'
    };

    static STATUS = {
        ACTIVE: 'ACTIVE',
        INACTIVE: 'INACTIVE',
        EXECUTED: 'EXECUTED',
        SCHEDULED: 'SCHEDULED',
        FAILED: 'FAILED',
    };
    static FREQUENCIES = {
        ONCE: 'ONCE',
        EVERY_DAY: 'EVERY_DAY',
        EVERY_WEEKDAY: 'EVERY_WEEKDAY',
        WEEKENDS: 'WEEKENDS'
    };
}