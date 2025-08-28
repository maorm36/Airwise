import WebApi from '../../../WebApi/WebApi';
import { TaskBoundary } from '../../../models/TaskBoundary';

export const useTaskHandlers = ({
    selectedAC,
    setTasks,
    closePanel,
    user,
    operator,
    tenant,
    showAlert
}) => {
    const handleAddTask = async (data) => {
        let createdTask = null;
        let isTaskBound = false;

        try {
            const taskBoundary = TaskBoundary.fromFormData(data, selectedAC, user, tenant);
            const createPayload = taskBoundary.getCreateTaskPayload(operator, tenant);
            createdTask = await WebApi.createTask(createPayload);

            if (!createdTask) {
                throw new Error('Task creation failed');
            }

            await WebApi.bindTaskToAC(
                selectedAC.id.systemID,
                selectedAC.id.objectId,
                { childId: { objectId: createdTask.id.objectId, systemID: createdTask.id.systemID } },
                operator.userId.systemID,
                operator.userId.email
            );

            isTaskBound = true;

            const schedulePayload = {
                acSystemID: createdTask.id.systemID,
                acObjectId: createdTask.id.objectId,
                taskDetails: taskBoundary.getTaskDetails(),
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email
            };

            await WebApi.scheduleTask(schedulePayload);

            const updatedTaskList = await WebApi.getTasksForAC({
                ACSystemID: selectedAC.id.systemID,
                ACId: selectedAC.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });

            setTasks(updatedTaskList || []);
            showAlert('Task created and scheduled successfully!', 'success');
        } catch (err) {
            console.error('Error in task creation flow:', err);
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                showAlert('Failed to create task: ' + err.message, 'error');
            }
            // Cleanup if task was created but not bound or scheduled
            if (createdTask) {
                try {
                    await WebApi.deleteEntityWithChildren({
                        entitySystemID: createdTask.id.systemID,
                        entityObjectId: createdTask.id.objectId,
                        operatorSystemID: user.userId.systemID,
                        operatorEmail: user.userId.email
                    });
                } catch (cleanupErr) {
                    if( cleanupErr.response && cleanupErr.response.data) {
                        showAlert(`Cleanup Error: ${cleanupErr.response.data.message}`, 'error');
                    } else {
                        console.error('Error during cleanup:', cleanupErr);
                    }   
                }
            }

        } finally {
            closePanel();
        }
    };

    const handleDeleteTask = async (task) => {
        try {
            await WebApi.deleteEntityWithChildren({
                entitySystemID: task.id.systemID,
                entityObjectId: task.id.objectId,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email
            });

            if (selectedAC) {
                const updatedTaskList = await WebApi.getTasksForAC({
                    ACSystemID: selectedAC.id.systemID,
                    ACId: selectedAC.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });
                setTasks(updatedTaskList || []);
            }

            showAlert('Task deleted successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                showAlert('Failed to delete task: ' + err.message, 'error');
            }
        }
    };

    const handleUpdateTask = async (task, updates) => {
        try {
            await WebApi.updateTask({
                taskSystemID: task.id.systemID,
                taskObjectId: task.id.objectId,
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email,
                updates: updates
            });

            if (selectedAC) {
                const updatedTaskList = await WebApi.getTasksForAC({
                    ACSystemID: selectedAC.id.systemID,
                    ACId: selectedAC.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });
                setTasks(updatedTaskList || []);
            }

            showAlert('Task updated successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                showAlert('Failed to update task: ' + err.message, 'error');
            }
        } finally {
            closePanel();
        }
    };

    const handleToggleTask = async (task) => {
        try {
            await WebApi.toggleTaskStatus({
                taskSystemID: task.id.systemID,
                taskObjectId: task.id.objectId,
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email
            });

            if (selectedAC) {
                const updatedTaskList = await WebApi.getTasksForAC({
                    ACSystemID: selectedAC.id.systemID,
                    ACId: selectedAC.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email
                });
                setTasks(updatedTaskList || []);
            }

            showAlert('Task status toggled successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                showAlert('Failed to toggle task status: ' + err.message, 'error');
            }
        }
    };

    // fetchTasks
    const fetchTasks = async () => {
        if (!selectedAC) {
            setTasks([]);
            return;
        }

        try {
            const taskList = await WebApi.getTasksForAC({
                ACSystemID: selectedAC.id.systemID,
                ACId: selectedAC.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });
            setTasks(taskList || []);
        } catch (err) {
            console.error('Error fetching tasks:', err);
            setTasks([]);
        }
    };

    return {
        handleAddTask,
        handleDeleteTask,
        handleUpdateTask,
        handleToggleTask,
        fetchTasks
    };

};
