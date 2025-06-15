export interface User {
    id: number;
    email: string;
    roles: string[];
}

export interface Admin extends User {
    fullName: string;
    phoneNumber?: string;
}

export interface Supplier extends User {
    supplierName: string;
    address: string;
    phoneNumber?: string;
    contactPerson?: string;
    category?: string;
    description?: string;
    status: SupplierStatus;
}

export enum SupplierStatus {
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE',
    BLACKLISTED = 'BLACKLISTED'
}

export interface Project {
    id: number;
    name: string;
    description?: string;
    startDate?: string;
    endDate?: string;
    status: ProjectStatus;
    createdAt: string;
    updatedAt: string;
    createdBy?: Admin;
}

export enum ProjectStatus {
    PLANNING = 'PLANNING',
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    ON_HOLD = 'ON_HOLD',
    CANCELLED = 'CANCELLED'
}

export interface Task {
    id: number;
    name: string;
    description?: string;
    status: TaskStatus;
    priority: TaskPriority;
    startDate?: string;
    dueDate?: string;
    project: Project;
    assignedTo?: Supplier;
    createdAt: string;
    updatedAt: string;
}

export enum TaskStatus {
    PENDING = 'PENDING',
    IN_PROGRESS = 'IN_PROGRESS',
    COMPLETED = 'COMPLETED',
    BLOCKED = 'BLOCKED'
}

export enum TaskPriority {
    LOW = 'LOW',
    MEDIUM = 'MEDIUM',
    HIGH = 'HIGH',
    URGENT = 'URGENT'
}

export interface ApiResponse<T> {
    message: string;
    success: boolean;
    data: T;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    userId: number;
    userType: 'admin' | 'supplier';
} 