import api from './api';


export async function createTenant(operatorSystemID, operatorEmail, userEmail) {
    const objectBoundary = {
        type: 'Tenant',
        alias: userEmail,
        active: true,
        status: 'completed',
        createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } }
    };
    const response = await api.post(
        '/objects',
        objectBoundary
    );
    return response.data;
}

export async function getTenant({ operatorSystemID, operatorEmail, tenantAlias }) {
    const tenants = await getTenants({ userSystemID: operatorSystemID, userEmail: operatorEmail, size: 1, page: 0, tenantAlias });
    return tenants[0] ?? null;
}

export async function getTenants({
    userSystemID,
    userEmail,
    page = 0,
    size = 10,
    tenantAlias
}) {
    const response = await api.get(
        `/objects/search/byAlias/${tenantAlias}`,
        {
            params: { userSystemID, userEmail, size, page }
        }
    );
    return response.data;
}


export default {
    createTenant,
    getTenant

};
