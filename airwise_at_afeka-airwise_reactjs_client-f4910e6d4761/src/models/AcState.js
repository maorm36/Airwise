export class AcState {
    constructor({ serial, power, temperature, mode, fanSpeed, manufacturer, motion }) {
        this.serial = serial;
        this.power = power;
        this.temperature = temperature;
        this.mode = mode;
        this.fanSpeed = fanSpeed;
        this.manufacturer = manufacturer;
        this.motion = motion;
    }

    // Convert AcState to flat command attributes structure required by server

    toCommandAttributes() {
        const attributes = {};

        if (this.power !== undefined) attributes.power = this.power;
        if (this.temperature !== undefined) attributes.temperature = this.temperature;
        if (this.mode !== undefined) attributes.mode = this.mode;
        if (this.fanSpeed !== undefined) attributes.fanSpeed = this.fanSpeed;

        return attributes;
    }

    // Create AcState from room preferences

    static fromRoomPreferences(roomPreferences, power = false) {
        return new AcState({
            serial: roomPreferences.serial || null,
            power: power,
            temperature: roomPreferences.temperature,
            mode: roomPreferences.mode,
            fanSpeed: roomPreferences.fanSpeed,
            manufacturer: roomPreferences.manufacturer || null,
            motion: roomPreferences.motion || false
        });
    }

    // Create AcState from AC object details

    static fromACDetails(acDetails, power = false) {
        return new AcState({
            serial: acDetails.serialNumber || acDetails.serial,
            power: power,
            temperature: acDetails.temperature,
            mode: acDetails.mode,
            fanSpeed: acDetails.fanSpeed,
            manufacturer: acDetails.manufacturer,
            motion: acDetails.motion || false
        });
    }

    // Validate the AC state values

    validate() {
        const errors = [];

        if (this.temperature !== undefined) {
            if (this.temperature < 16 || this.temperature > 30) {
                errors.push('Temperature must be between 16-30 degrees Celsius');
            }
        }

        if (this.mode !== undefined) {
            const validModes = ['cool', 'heat', 'dry', 'fan', 'auto'];
            if (!validModes.includes(this.mode.toLowerCase())) {
                errors.push(`Mode must be one of: ${validModes.join(', ')}`);
            }
        }

        if (this.fanSpeed !== undefined) {
            const validSpeeds = ['low', 'medium', 'high', 'auto'];
            if (!validSpeeds.includes(this.fanSpeed.toLowerCase())) {
                errors.push(`Fan speed must be one of: ${validSpeeds.join(', ')}`);
            }
        }

        return {
            valid: errors.length === 0,
            errors: errors
        };
    }
}