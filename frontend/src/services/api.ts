import axios from 'axios';
import { ApiResponse, LoginRequest, LoginResponse, Project, Task, Admin, Supplier } from '../types';

const baseURL = 'http://localhost:8082/api';

const api = axios.create({
    baseURL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to include the auth token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Add response interceptor for better error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.code === 'ERR_NETWORK') {
            console.error('Network Error: Please check if the backend server is running at', baseURL);
            // You might want to show a more user-friendly error message
            error.message = 'Unable to connect to the server. Please check if the backend is running.';
        }
        return Promise.reject(error);
    }
);

// Auth API
export const authAPI = {
    login: (data: LoginRequest) => 
        api.post<ApiResponse<LoginResponse>>('/auth/login', data),
    
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
        api.get<ApiResponse<{ content: Task[]; totalElements: number }>>(`/tasks?page=${page}&size=${size}`),
    
    getTaskById: (id: number) => 
        api.get<ApiResponse<Task>>(`/tasks/${id}`),
    
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
        api.get<ApiResponse<{ content: Task[]; totalElements: number }>>(`/tasks/project/${projectId}?page=${page}&size=${size}`),
    
    getMyTasks: () => 
        api.get<ApiResponse<Task[]>>('/tasks/my-tasks'),
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