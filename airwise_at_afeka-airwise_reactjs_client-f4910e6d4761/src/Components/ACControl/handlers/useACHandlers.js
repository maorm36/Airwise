import WebApi from '../../../WebApi/WebApi';
import { AcConstants } from '../../Utils/AcConstants';

export const useACHandlers = ({
    operator,
    user,
    selectedRoom,
    refreshACs,
    closePanel,
    fetchTasks,
    showAlert
}) => {
    const handleAddAC = async (data) => {
        try {

            if (!selectedRoom) {
                throw new Error('No room selected for AC creation');
            }

            const verificationResult = await WebApi.verifyACBySerialThenAdd({
                acSystemID: selectedRoom.id.systemID,
                acObjectId: selectedRoom.id.objectId,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email,
                acData: data,
            });

            await refreshACs();
            showAlert('AC unit created and verified successfully!', 'success');
        } catch (err) {
            console.error('Error in AC creation flow:', err.response || err);
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error creating AC unit', err);
                showAlert('Failed to create AC unit', 'error');
            }
        } finally {
            closePanel();
        }
    };

    const handleDeleteAC = async (ac) => {
        try {
            await WebApi.deleteEntityWithChildren({
                entitySystemID: ac.id.systemID,
                entityObjectId: ac.id.objectId,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email
            });

            await refreshACs();
            await fetchTasks();

            showAlert('AC unit deleted successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error deleting AC unit', err);
                showAlert('Failed to delete AC unit', 'error');
            }
        }
    };

    const handleToggleACPower = async (ac) => {
        try {
            const newStatus =
                ac.status === AcConstants.STATUS.ON ? AcConstants.STATUS.OFF : AcConstants.STATUS.ON;

            await WebApi.updateACState({
                acSystemID: ac.id.systemID,
                acObjectId: ac.id.objectId,
                newState: newStatus,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email,
                preferences: ac.objectDetails
            });

            await refreshACs();

            showAlert(`AC unit turned ${newStatus.toLowerCase()}`, 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error toggling AC power', err);
                showAlert('Failed to toggle AC power', 'error');
            }
        }
    };

    const handleEditACPreferences = async (ac, preferences) => {
        try {
            await WebApi.updateACPreferences({
                acSystemID: ac.id.systemID,
                acObjectId: ac.id.objectId,
                preferences: preferences,
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email
            });

            if (ac.status === AcConstants.STATUS.ON) {
                showAlert('Preferences updated and applied to running AC', 'success');
            } else {
                showAlert('AC preferences updated successfully', 'success');
            }

            await refreshACs();
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error updating AC preferences', err);
                showAlert('Failed to update AC preferences', 'error');
            }
        } finally {
            closePanel();
        }
    };

    return {
        handleAddAC,
        handleDeleteAC,
        handleToggleACPower,
        handleEditACPreferences
    };
};
