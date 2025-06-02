import React, { useEffect, useState } from 'react';
import { Grid, Paper, Typography, Box, CircularProgress, Alert } from '@mui/material';
import { projectAPI, taskAPI, supplierAPI } from '../services/api';
import { Project, Task, Supplier } from '../types';

interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  pendingTasks: number;
  totalSuppliers: number;
  activeSuppliers: number;
}

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<DashboardStats>({
    totalProjects: 0,
    activeProjects: 0,
    totalTasks: 0,
    pendingTasks: 0,
    totalSuppliers: 0,
    activeSuppliers: 0
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      setError(null);
      try {
        // Fetch all data in parallel
        const [projectsResponse, tasksResponse, suppliersResponse] = await Promise.all([
          projectAPI.getAllProjects(0, 1000),
          taskAPI.getAllTasks(0, 1000),
          supplierAPI.getAllSuppliers(0, 1000)
        ]);

        // Check if responses are successful
        if (!projectsResponse.data.success || !tasksResponse.data.success || !suppliersResponse.data.success) {
          throw new Error('Failed to fetch dashboard data');
        }

        const projects = projectsResponse.data.data.content || [];
        const tasks = tasksResponse.data.data.content || [];
        const suppliers = suppliersResponse.data.data.content || [];

        console.log('Dashboard Data:', { projects, tasks, suppliers }); // Debug log

        setStats({
          totalProjects: projects.length,
          activeProjects: projects.filter(p => p.status === 'ACTIVE').length,
          totalTasks: tasks.length,
          pendingTasks: tasks.filter(t => t.status === 'PENDING').length,
          totalSuppliers: suppliers.length,
          activeSuppliers: suppliers.filter(s => s.status === 'ACTIVE').length
        });
      } catch (error: any) {
        console.error('Error fetching dashboard data:', error);
        setError(error.response?.data?.message || 'Failed to load dashboard data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Dashboard Overview
      </Typography>
      
      <Grid container spacing={3}>
        {/* Projects Stats */}
        <Grid item xs={12} md={4}>
          <Paper 
            sx={{ 
              p: 3, 
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              textAlign: 'center'
            }}
          >
            <Typography variant="h6" gutterBottom color="primary">
              Projects Overview
            </Typography>
            <Typography variant="h3" color="primary" sx={{ my: 2 }}>
              {stats.totalProjects}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Total Projects
            </Typography>
            <Typography variant="h5" color="success.main" sx={{ mt: 2 }}>
              {stats.activeProjects}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Active Projects
            </Typography>
          </Paper>
        </Grid>

        {/* Tasks Stats */}
        <Grid item xs={12} md={4}>
          <Paper 
            sx={{ 
              p: 3, 
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              textAlign: 'center'
            }}
          >
            <Typography variant="h6" gutterBottom color="primary">
              Tasks Overview
            </Typography>
            <Typography variant="h3" color="primary" sx={{ my: 2 }}>
              {stats.totalTasks}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Total Tasks
            </Typography>
            <Typography variant="h5" color="warning.main" sx={{ mt: 2 }}>
              {stats.pendingTasks}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Pending Tasks
            </Typography>
          </Paper>
        </Grid>

        {/* Suppliers Stats */}
        <Grid item xs={12} md={4}>
          <Paper 
            sx={{ 
              p: 3, 
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              textAlign: 'center'
            }}
          >
            <Typography variant="h6" gutterBottom color="primary">
              Suppliers Overview
            </Typography>
            <Typography variant="h3" color="primary" sx={{ my: 2 }}>
              {stats.totalSuppliers}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Total Suppliers
            </Typography>
            <Typography variant="h5" color="success.main" sx={{ mt: 2 }}>
              {stats.activeSuppliers}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Active Suppliers
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard; 