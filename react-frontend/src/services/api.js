import axios from 'axios';

const API = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

const unwrapData = (response) => {
    const payload = response?.data;

    // User and category endpoints use ApiResponse<T>, while transaction endpoints return T directly.
    if (payload && typeof payload === 'object' && Object.hasOwn(payload, 'data')) {
        return payload.data;
    }

    return payload;
};

export const TransactionApi = {
    getTransactionsByUser: async (userId, year, month) => {
        const response = await API.get(`/transaction/user/${userId}`, { params: { year, month } });
        return unwrapData(response);
    },
    getRecentTransactions: async (userId, startPage = 0, endPage = 0, size = 5) => {
        const response = await API.get(`/transaction/recent/user/${userId}`, {
            params: { startPage, endPage, size },
        });
        return unwrapData(response);
    },
    getDistinctTransactionYears: async (userId) => {
        const response = await API.get(`/transaction/years/${userId}`);
        return unwrapData(response);
    },
    searchTransactions: async (userId, categoryId, transactionType, minAmount, maxAmount) => {
        const params = {};
        if (categoryId) params.categoryId = categoryId;
        if (transactionType) params.transactionType = transactionType;
        if (minAmount) params.minAmount = minAmount;
        if (maxAmount) params.maxAmount = maxAmount;
        
        const response = await API.get(`/transaction/search/user/${userId}`, { params });
        return unwrapData(response);
    },
    getSpendingBreakdown: async (userId) => {
        const response = await API.get(`/transaction/breakdown/user/${userId}`);
        return unwrapData(response);
    },
    createTransaction: async (transaction) => {
        const response = await API.post('/transaction', transaction);
        return unwrapData(response);
    },
    updateTransaction: async (transaction) => {
        const response = await API.put('/transaction', transaction);
        return unwrapData(response);
    },
    deleteTransaction: async (transactionId) => {
        await API.delete(`/transaction/${transactionId}`);
    },
};

export const CategoryApi = {
    getCategoriesByUser: async (userId) => {
        const response = await API.get(`/transaction-categories/user/${userId}`);
        return unwrapData(response);
    },
    getCategoryById: async (id) => {
        const response = await API.get(`/transaction-categories/${id}`);
        return unwrapData(response);
    },
    createCategory: async (category) => {
        const response = await API.post('/transaction-categories', category);
        return unwrapData(response);
    },
    updateCategory: async (id, category) => {
        const response = await API.put(`/transaction-categories/${id}`, category);
        return unwrapData(response);
    },
    deleteCategory: async (id) => {
        await API.delete(`/transaction-categories/${id}`);
    },
};

export const UserApi = {
    getUserById: async (userId) => {
        const response = await API.get(`/users/${userId}`);
        return unwrapData(response);
    },
    getUserByEmail: async (email) => {
        const response = await API.get(`/users/email/${encodeURIComponent(email)}`);
        return unwrapData(response);
    },
    createUser: async (user) => {
        const response = await API.post('/users', user);
        return unwrapData(response);
    },
};

export const AuthApi = {
    googleSignIn: async (idToken) => {
        const response = await API.post('/auth/google', { idToken });
        return unwrapData(response);
    },
};

export default API;