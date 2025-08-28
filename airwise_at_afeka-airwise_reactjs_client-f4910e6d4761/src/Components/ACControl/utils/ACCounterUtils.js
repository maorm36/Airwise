import { AcConstants } from "../../Utils/AcConstants";

export async function countActiveACsInRoom(room, user, webApi) {
    try {

        const acs = await webApi.getACsForRoom({
            roomSystemID: room.id.systemID,
            roomId: room.id.objectId,
            userSystemID: user.userId.systemID,
            userEmail: user.userId.email
        });

        // Count ACs that are 'on'
        const activeCount = acs.filter(ac => {
            const isOn = ac.status === AcConstants.ACTIONS.TURN_ON;
            return isOn;
        }).length;

        return activeCount;
    } catch (err) {
        return 0; // Return 0 on error instead of throwing
    }
}

// Count ACs with status 'on' in all rooms under a site
export async function countActiveACsInSite(site, user, webApi) {
    try {

        // Get all rooms in the site
        const rooms = await webApi.getRoomsForSite({
            siteSystemID: site.id.systemID,
            siteId: site.id.objectId,
            userSystemID: user.userId.systemID,
            userEmail: user.userId.email
        });


        if (!rooms || rooms.length === 0) {
            return 0;
        }

        let totalActiveACs = 0;

        // Count active ACs in each room (sequential to avoid overwhelming the server)
        for (const room of rooms) {
            try {
                const roomActiveACs = await countActiveACsInRoom(room, user, webApi);
                totalActiveACs += roomActiveACs;
            } catch (err) {
                // Continue with other rooms even if one fails
            }
        }

        return totalActiveACs;

    } catch (err) {
        return 0;
    }
}

// Batch count active ACs for multiple rooms with error handling
export async function countActiveACsForRooms(rooms, user, webApi) {

    const roomCounts = {};

    // Use sequential processing to avoid overwhelming the server
    for (const room of rooms) {
        try {
            const count = await countActiveACsInRoom(room, user, webApi);
            roomCounts[room.id.objectId] = count;
        } catch (err) {
            roomCounts[room.id.objectId] = 0;
        }
    }

    return roomCounts;
}

// Batch count active ACs for multiple sites with error handling
export async function countActiveACsForSites(sites, user, webApi) {
 
    const siteCounts = {};

    // Use sequential processing to avoid overwhelming the server
    for (const site of sites) {
        try {
            const count = await countActiveACsInSite(site, user, webApi);
            siteCounts[site.id.objectId] = count;
        } catch (err) {
            siteCounts[site.id.objectId] = 0;
        }
    }

    return siteCounts;
}