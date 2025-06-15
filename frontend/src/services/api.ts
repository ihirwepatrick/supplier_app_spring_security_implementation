import axios from 'axios';
import { ApiResponse, LoginRequest, LoginResponse, Project, Task, Admin, Supplier } from '../types';

const baseURL = 'http://localhost:8082/api';

// Create axios instance with default config
const api = axios.create({
    baseURL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    // Add timeout to prevent hanging requests
    timeout: 10000
});

// Add a request interceptor to include the auth token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    
    // Log the full request details
    console.log('API Request Details:', {
        url: `${baseURL}${config.url}`,
        method: config.method?.toUpperCase(),
        headers: config.headers,
        data: config.data,
        hasToken: !!token,
        tokenLength: token?.length
    });

    if (token) {
        // Always ensure Bearer prefix
        const authToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
        config.headers.Authorization = authToken;
        
        // Log token details (safely)
        console.log('Auth Token Details:', {
            prefix: authToken.substring(0, 7),
            length: authToken.length,
            lastChars: '...' + authToken.slice(-4)
        });
    } else {
        // Only warn for protected endpoints
        if (!config.url?.includes('/auth/')) {
            console.warn('No auth token found for protected endpoint:', config.url);
        }
    }

    return config;
}, (error) => {
    console.error('Request Interceptor Error:', error);
    return Promise.reject(error);
});

// Add response interceptor for better error handling
api.interceptors.response.use(
    (response) => {
        // Log successful responses
        console.log('API Response Success:', {
            url: response.config.url,
            method: response.config.method?.toUpperCase(),
            status: response.status,
            statusText: response.statusText,
            hasData: !!response.data,
            dataKeys: response.data ? Object.keys(response.data) : []
        });
        return response;
    },
    (error) => {
        // Enhanced error logging
        const errorDetails = {
            url: error.config?.url,
            method: error.config?.method?.toUpperCase(),
            status: error.response?.status,
            statusText: error.response?.statusText,
            data: error.response?.data,
            message: error.message,
            headers: error.config?.headers,
            hasAuthHeader: !!error.config?.headers?.Authorization
        };

        console.error('API Error Details:', errorDetails);

        // Handle specific error cases
        if (error.response?.status === 401 || error.response?.status === 403) {
            const isAuthEndpoint = error.config?.url?.includes('/auth/');
            
            // Only clear auth data and redirect if it's not an auth endpoint
            if (!isAuthEndpoint) {
                console.warn('Session expired or invalid - clearing auth data');
                // Clear auth data
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                
                // Only redirect if we're not already on the login page
                if (!window.location.pathname.includes('/login')) {
                    console.log('Redirecting to login page...');
                    window.location.href = '/login';
                }
            }
        } else if (error.code === 'ERR_NETWORK') {
            console.error('Network Error - Backend server may be down:', {
                url: baseURL,
                message: error.message
            });
            error.message = 'Unable to connect to the server. Please check if the backend is running.';
        }

        return Promise.reject(error);
    }
);

// Auth API with enhanced error handling
export const authAPI = {
    login: async (data: LoginRequest) => {
        try {
            // Clear any existing auth data before login
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            
            const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', data);
            console.log('Login Response:', {
                success: response.data.success,
                hasToken: !!response.data.data?.token,
                userType: response.data.data?.userType
            });
            return response;
        } catch (error: any) {
            console.error('Login Error:', {
                status: error.response?.status,
                message: error.response?.data?.message || error.message
            });
            throw error;
        }
    },
    
    registerAdmin: (data: Partial<Admin>) => 
        api.post<ApiResponse<Admin>>('/auth/admin/register', data),
    
    registerSupplier: (data: Partial<Supplier>) => 
        api.post<ApiResponse<Supplier>>('/auth/supplier/register', data),
};

// Project API
export const projectAPI = {
    getAllProjects: (page = 0, size = 10) => 
        api.get<ApiResponse<{ content: Project[]; totalElements: number }>>(`/projects?page=${page}&size=${size}`),
    
    getProjectById: (id: number) => 
        api.get<ApiResponse<Project>>(`/projects/${id}`),
    
    createProject: (data: Partial<Project>) => 
        api.post<ApiResponse<Project>>('/projects', data),
    
    updateProject: (id: number, data: Partial<Project>) => 
        api.put<ApiResponse<Project>>(`/projects/${id}`, data),
    
    deleteProject: (id: number) => 
        api.delete<ApiResponse<void>>(`/projects/${id}`),
    
    updateProjectStatus: (id: number, status: string) => 
        api.patch<ApiResponse<Project>>(`/projects/${id}/status`, { status }),
    
    getMyProjects: () => 
        api.get<ApiResponse<Project[]>>('/projects/my-projects'),
    
    searchProjects: (name: string, page = 0, size = 10) => 
        api.get<ApiResponse<{ content: Project[]; totalElements: number }>>(`/projects/search?name=${name}&page=${page}&size=${size}`),
};

// Task API
export const taskAPI = {
    getAllTasks: (page = 0, size = 10) => 
        api.get<ApiResponse<{ content: Task[]; totalElements: number }>>(`/tasks?page=${page}&size=${size}&fetch=project`),
    
    getTaskById: (id: number) => 
        api.get<ApiResponse<Task>>(`/tasks/${id}?fetch=project`),
    
    createTask: (projectId: number, data: Partial<Task>) => 
        api.post<ApiResponse<Task>>(`/tasks/project/${projectId}`, data),
    
    updateTask: (id: number, data: Partial<Task>) => 
        api.put<ApiResponse<Task>>(`/tasks/${id}`, data),
    
    deleteTask: (id: number) => 
        api.delete<ApiResponse<void>>(`/tasks/${id}`),
    
    assignTaskToSupplier: (taskId: number, supplierId: number) => 
        api.patch<ApiResponse<Task>>(`/tasks/${taskId}/assign/${supplierId}`),
    
    updateTaskStatus: (id: number, status: string) => 
        api.patch<ApiResponse<Task>>(`/tasks/${id}/status`, { status }),
    
    getTasksByProject: (projectId: number, page = 0, size = 10) => 
        api.get<ApiResponse<{ content: Task[]; totalElements: number }>>(`/tasks/project/${projectId}?page=${page}&size=${size}&fetch=project`),
    
    getMyTasks: () => 
        api.get<ApiResponse<Task[]>>('/tasks/my-tasks?fetch=project'),
};

export const supplierAPI = {
    getAllSuppliers: (page = 0, size = 10) => 
        api.get<ApiResponse<{ content: Supplier[]; totalElements: number }>>(`/suppliers?page=${page}&size=${size}`),
    
    getSupplierById: (id: number) => 
        api.get<ApiResponse<Supplier>>(`/suppliers/${id}`),
    
    createSupplier: (data: Partial<Supplier>) => 
        api.post<ApiResponse<Supplier>>('/suppliers', data),
    
    updateSupplier: (id: number, data: Partial<Supplier>) => 
        api.put<ApiResponse<Supplier>>(`/suppliers/${id}`, data),
    
    deleteSupplier: (id: number) => 
        api.delete<ApiResponse<void>>(`/suppliers/${id}`),
    
    searchSuppliers: (name: string) => 
        api.get<ApiResponse<Supplier[]>>(`/suppliers/search?name=${name}`),
    
    getSuppliersByStatus: (status: string) => 
        api.get<ApiResponse<Supplier[]>>(`/suppliers/status/${status}`),
    
    getSuppliersByCategory: (category: string) => 
        api.get<ApiResponse<Supplier[]>>(`/suppliers/category/${category}`),
};

export default api; 