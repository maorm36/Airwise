import WebApi from '../../../WebApi/WebApi';

export const useSiteHandlers = ({
    operator, tenant, user,
    refreshSites, closePanel,
    refreshRooms, refreshACs, fetchTasks,
    showAlert
}) => {

    const handleAddSite = async (data) => {
        try {
            const createdSite = await WebApi.createSite({
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email,
                site: data
            });

            if (!createdSite) {
                throw new Error('Site creation failed');
            }

            await WebApi.bindSiteToTenant(
                tenant.id.systemID,
                tenant.id.objectId,
                { childId: { objectId: createdSite.id.objectId, systemID: createdSite.id.systemID } },
                operator.userId.systemID,
                operator.userId.email
            );

            await refreshSites();
            showAlert('Site created successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error creating site', err);
                showAlert('Failed to create site', 'error');
            }
        } finally {
            closePanel();
        }
    };

    const handleDeleteSite = async (site) => {
        try {

            await WebApi.deleteEntityWithChildren({
                entitySystemID: site.id.systemID,
                entityObjectId: site.id.objectId,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email
            });

            await refreshSites();
            await refreshRooms();
            await refreshACs();
            await fetchTasks();

            showAlert('Site deleted successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error deleting site', err);
                showAlert('Failed to delete site', 'error');
            }
        }
    };

    const handleUpdateSite = async (site, name) => {
        try {
            const updatedData = {
                type: site.type,
                alias: name,
                active: true,
                status: site.status,
                objectDetails: site.objectDetails,
                createdBy: site.createdBy
            };

            await WebApi.updateSite(
                site.id.systemID,
                site.id.objectId,
                site.createdBy.userId.systemID,
                site.createdBy.userId.email,
                updatedData
            );

            await refreshSites();
            showAlert('Site updated successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error updating site', err);
                showAlert('Failed to update site', 'error');
            }
        } finally {
            closePanel();
        }
    };

    return {
        handleAddSite,
        handleDeleteSite,
        handleUpdateSite
    };
};
