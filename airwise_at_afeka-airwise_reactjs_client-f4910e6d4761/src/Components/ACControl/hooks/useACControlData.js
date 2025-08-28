import { useState, useEffect } from 'react';
import WebApi from '../../../WebApi/WebApi';
import { countActiveACsForSites, countActiveACsForRooms } from '../utils/ACCounterUtils';

export const useACControlData = () => {
    const user = JSON.parse(localStorage.getItem('user'));
    const operator = JSON.parse(localStorage.getItem('operator'));
    const tenant = JSON.parse(localStorage.getItem('tenant'));

    const [sites, setSites] = useState([]);
    const [rooms, setRooms] = useState([]);
    const [acUnits, setAcUnits] = useState([]);
    const [tasks, setTasks] = useState([]);

    const [siteACCounts, setSiteACCounts] = useState({});
    const [roomACCounts, setRoomACCounts] = useState({});
    const [isCountingACs, setIsCountingACs] = useState(false);

    const [selectedSite, setSelectedSite] = useState(null);
    const [selectedRoom, setSelectedRoom] = useState(null);
    const [selectedAC, setSelectedAC] = useState(null);

    useEffect(() => {
        const fetchSites = async () => {
            try {

                const list = await WebApi.getSitesForTenant({
                    tenantSystemID: tenant.id.systemID,
                    tenantId: tenant.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });

                setSites(list || []);

                if (list && list.length > 0) {
                    setIsCountingACs(true);

                    try {
                        const counts = await countActiveACsForSites(list, user, WebApi);
                        setSiteACCounts(counts);
                    } catch (err) {
                        console.error('Error counting site ACs:', err);
                        setSiteACCounts({});
                    } finally {
                        setIsCountingACs(false);
                    }
                } else {
                    setSiteACCounts({});
                }

            } catch (err) {
                console.error('Error loading sites', err);
                setSites([]);
                setSiteACCounts({});
            }
        };

        if (user && operator && tenant) {
            fetchSites();
        }
    }, []);

    useEffect(() => {
        if (!selectedSite) {
            setRooms([]);
            setAcUnits([]);
            setTasks([]);
            setRoomACCounts({});
            return;
        }

        const fetchRooms = async () => {
            try {

                const list = await WebApi.getRoomsForSite({
                    siteSystemID: selectedSite.id.systemID,
                    siteId: selectedSite.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });

                setRooms(list || []);

                if (list && list.length > 0) {
                    setIsCountingACs(true);

                    try {
                        const counts = await countActiveACsForRooms(list, user, WebApi);
                        setRoomACCounts(counts);
                    } catch (err) {
                        setRoomACCounts({});
                    } finally {
                        setIsCountingACs(false);
                    }
                } else {
                    setRoomACCounts({});
                }

            } catch (err) {
                setRooms([]);
                setRoomACCounts({});
            }
        };
        fetchRooms();
    }, [selectedSite]);

    useEffect(() => {
        if (!selectedRoom) {
            setAcUnits([]);
            setTasks([]);
            return;
        }

        const fetchACs = async () => {
            try {

                const acData = await WebApi.getACsForRoom({
                    roomSystemID: selectedRoom.id.systemID,
                    roomId: selectedRoom.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });

                setAcUnits(acData || []);

            } catch (err) {
                setAcUnits([]);
            }
        };
        fetchACs();
    }, [selectedRoom]);

    useEffect(() => {
        if (!selectedAC) {
            setTasks([]);
            return;
        }
        const fetchTasks = async () => {
            try {

                const taskList = await WebApi.getTasksForAC({
                    ACSystemID: selectedAC.id.systemID,
                    ACId: selectedAC.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                }
                );

                setTasks(taskList || []);
            } catch (err) {
                setTasks([]);
            }
        };
        fetchTasks();
    }, [selectedAC]);
    

    const refreshSites = async () => {
        try {

            const list = await WebApi.getSitesForTenant({
                tenantSystemID: tenant.id.systemID,
                tenantId: tenant.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });
            setSites(list);

            if (list && list.length > 0) {
                setIsCountingACs(true);
                try {
                    const counts = await countActiveACsForSites(list, user, WebApi);
                    setSiteACCounts(counts);
                } catch (err) {
                    setSiteACCounts({});
                } finally {
                    setIsCountingACs(false);
                }
            }
        } catch (err) {
            console.error('Error refreshing sites', err);
        }
    };

    const refreshRooms = async () => {
        if (!selectedSite) return;
        try {

            const list = await WebApi.getRoomsForSite({
                siteSystemID: selectedSite.id.systemID,
                siteId: selectedSite.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });
            setRooms(list);

            if (list && list.length > 0) {
                setIsCountingACs(true);
                try {
                    const counts = await countActiveACsForRooms(list, user, WebApi);
                    setRoomACCounts(counts);
                } catch (err) {
                    setRoomACCounts({});
                } finally {
                    setIsCountingACs(false);
                }
            }
        } catch (err) {
            console.error('Error refreshing rooms', err);
        }
    };

    const refreshACs = async () => {
        if (!selectedRoom) return;
        try {

            const list = await WebApi.getACsForRoom({
                roomSystemID: selectedRoom.id.systemID,
                roomId: selectedRoom.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });

            setAcUnits(list || []);

            setTimeout(async () => {
                try {
                    if (selectedSite) {
                        await refreshRooms();
                    }
                    await refreshSites();
                } catch (err) {
                    console.error('Error refreshing counts after AC change:', err);
                }
            }, 100);

        } catch (err) {
            console.error('Error refreshing ACs', err);
            setAcUnits([]);
        }
    };

    return {
        // Data
        sites, rooms, acUnits, tasks,
        // AC counts
        siteACCounts, roomACCounts,
        // Loading state
        isCountingACs,
        // Selection state
        selectedSite, selectedRoom, selectedAC,
        // Setters
        setSelectedSite, setSelectedRoom, setSelectedAC,
        // Data setters
        setSites, setRooms, setAcUnits, setTasks,
        // Refresh functions
        refreshSites, refreshRooms, refreshACs,
        // User data
        user, operator, tenant
    };
};