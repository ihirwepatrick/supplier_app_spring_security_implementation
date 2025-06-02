import React, { useEffect, useState } from 'react';
import { Grid, Paper, Typography, Box, CircularProgress } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { projectAPI, taskAPI, supplierAPI } from '../services/api';
import { Project, Task, Supplier } from '../types';

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalProjects: 0,
    activeProjects: 0,
    totalTasks: 0,
    pendingTasks: 0,
    totalSuppliers: 0,
    activeSuppliers: 0
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch projects
        const projectsResponse = await projectAPI.getAllProjects(0, 1000);
        const projects = projectsResponse.data.data.content;
        
        // Fetch tasks
        const tasksResponse = await taskAPI.getAllTasks(0, 1000);
        const tasks = tasksResponse.data.data.content;
        
        // Fetch suppliers
        const suppliersResponse = await supplierAPI.getAllSuppliers(0, 1000);
        const suppliers = suppliersResponse.data.data.content;

        setStats({
          totalProjects: projects.length,
          activeProjects: projects.filter(p => p.status === 'ACTIVE').length,
          totalTasks: tasks.length,
          pendingTasks: tasks.filter(t => t.status === 'PENDING').length,
          totalSuppliers: suppliers.length,
          activeSuppliers: suppliers.filter(s => s.status === 'ACTIVE').length
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
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

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Welcome, {user?.fullName || 'Admin'}
      </Typography>
      
      <Grid container spacing={3}>
        {/* Projects Stats */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Projects Overview
            </Typography>
            <Typography variant="h3" color="primary">
              {stats.totalProjects}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Active Projects: {stats.activeProjects}
            </Typography>
          </Paper>
        </Grid>

        {/* Tasks Stats */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Tasks Overview
            </Typography>
            <Typography variant="h3" color="primary">
              {stats.totalTasks}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Pending Tasks: {stats.pendingTasks}
            </Typography>
          </Paper>
        </Grid>

        {/* Suppliers Stats */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Suppliers Overview
            </Typography>
            <Typography variant="h3" color="primary">
              {stats.totalSuppliers}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Active Suppliers: {stats.activeSuppliers}
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard; 