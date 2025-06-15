import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  useTheme,
  alpha,
} from '@mui/material';
import {
  PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, BarChart, Bar
} from 'recharts';
import { format, subMonths, startOfMonth, endOfMonth } from 'date-fns';
import {
  Assignment as ProjectIcon,
  Task as TaskIcon,
  People as SupplierIcon,
  TrendingUp as TrendingIcon,
} from '@mui/icons-material';
import { projectAPI, taskAPI, supplierAPI } from '../services/api';
import { Project, Task, TaskStatus, ProjectStatus } from '../types';

interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  completedProjects: number;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  totalSuppliers: number;
  activeSuppliers: number;
}

interface ProjectStatusData {
  name: string;
  value: number;
}

interface TaskTrendData {
  date: string;
  completed: number;
  pending: number;
}

interface ProjectTimelineData {
  name: string;
  startDate: string;
  endDate: string;
  progress: number;
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

const StatCard: React.FC<{
  title: string;
  value: number | string;
  subtitle: string;
  icon: React.ReactNode;
  color: string;
  trend?: { value: number; label: string };
}> = ({ title, value, subtitle, icon, color, trend }) => {
  const theme = useTheme();
  
  return (
    <Card
      sx={{
        position: 'relative',
        overflow: 'visible',
        '&:hover': {
          transform: 'translateY(-4px)',
          transition: 'transform 0.3s ease-in-out',
        },
        transition: 'all 0.3s ease-in-out',
        background: `linear-gradient(135deg, ${alpha(color, 0.1)} 0%, ${alpha(color, 0.05)} 100%)`,
        border: `1px solid ${alpha(color, 0.2)}`,
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Box
          sx={{
            position: 'absolute',
            top: -20,
            right: 20,
            width: 48,
            height: 48,
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: `linear-gradient(135deg, ${color} 0%, ${alpha(color, 0.8)} 100%)`,
            boxShadow: `0 4px 12px ${alpha(color, 0.3)}`,
          }}
        >
          {icon}
        </Box>
        
        <Box sx={{ mt: 2 }}>
          <Typography
            variant="h6"
            sx={{
              color: 'text.secondary',
              fontSize: '0.875rem',
              fontWeight: 500,
              mb: 1,
            }}
          >
            {title}
          </Typography>
          
          <Typography
            variant="h3"
            sx={{
              color: 'text.primary',
              fontWeight: 600,
              mb: 1,
              fontSize: { xs: '1.75rem', sm: '2rem' },
            }}
          >
            {value}
          </Typography>
          
          <Typography
            variant="body2"
            sx={{
              color: 'text.secondary',
              display: 'flex',
              alignItems: 'center',
              gap: 1,
            }}
          >
            {subtitle}
            {trend && (
              <Box
                component="span"
                sx={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  color: trend.value >= 0 ? 'success.main' : 'error.main',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  ml: 1,
                }}
              >
                <TrendingIcon
                  sx={{
                    fontSize: '1rem',
                    transform: trend.value >= 0 ? 'rotate(0deg)' : 'rotate(180deg)',
                  }}
                />
                {trend.value}%
              </Box>
            )}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

const Dashboard: React.FC = () => {
  const theme = useTheme();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<DashboardStats>({
    totalProjects: 0,
    activeProjects: 0,
    completedProjects: 0,
    totalTasks: 0,
    completedTasks: 0,
    pendingTasks: 0,
    totalSuppliers: 0,
    activeSuppliers: 0,
  });
  const [projectStatusData, setProjectStatusData] = useState<ProjectStatusData[]>([]);
  const [taskTrendData, setTaskTrendData] = useState<TaskTrendData[]>([]);
  const [projectTimelineData, setProjectTimelineData] = useState<ProjectTimelineData[]>([]);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const [projectsRes, tasksRes, suppliersRes] = await Promise.all([
          projectAPI.getAllProjects(0, 1000),
          taskAPI.getAllTasks(0, 1000),
          supplierAPI.getAllSuppliers(0, 1000),
        ]);

        if (!projectsRes.data.success || !tasksRes.data.success || !suppliersRes.data.success) {
          throw new Error('Failed to fetch dashboard data');
        }

        const projects = projectsRes.data.data.content;
        const tasks = tasksRes.data.data.content;
        const suppliers = suppliersRes.data.data.content;

        // Calculate statistics
        const stats: DashboardStats = {
          totalProjects: projects.length,
          activeProjects: projects.filter(p => p.status === ProjectStatus.IN_PROGRESS).length,
          completedProjects: projects.filter(p => p.status === ProjectStatus.COMPLETED).length,
          totalTasks: tasks.length,
          completedTasks: tasks.filter(t => t.status === TaskStatus.COMPLETED).length,
          pendingTasks: tasks.filter(t => t.status === TaskStatus.PENDING).length,
          totalSuppliers: suppliers.length,
          activeSuppliers: suppliers.filter(s => s.status === 'ACTIVE').length,
        };
        setStats(stats);

        // Prepare project status data for pie chart
        const statusCount = projects.reduce((acc, project) => {
          acc[project.status] = (acc[project.status] || 0) + 1;
          return acc;
        }, {} as Record<string, number>);

        const projectStatusData = Object.entries(statusCount).map(([name, value]) => ({
          name,
          value,
        }));
        setProjectStatusData(projectStatusData);

        // Prepare task trend data for line chart
        const last6Months = Array.from({ length: 6 }, (_, i) => {
          const date = subMonths(new Date(), i);
          return {
            date: format(date, 'MMM yyyy'),
            start: startOfMonth(date),
            end: endOfMonth(date),
          };
        }).reverse();

        const taskTrendData = last6Months.map(({ date, start, end }) => {
          const monthTasks = tasks.filter(task => {
            const taskDate = new Date(task.createdAt);
            return taskDate >= start && taskDate <= end;
          });

          return {
            date,
            completed: monthTasks.filter(t => t.status === TaskStatus.COMPLETED).length,
            pending: monthTasks.filter(t => t.status !== TaskStatus.COMPLETED).length,
          };
        });
        setTaskTrendData(taskTrendData);

        // Prepare project timeline data
        const projectTimelineData = projects
          .filter(p => p.status !== ProjectStatus.COMPLETED)
          .map(project => ({
            name: project.name,
            startDate: project.startDate,
            endDate: project.endDate,
            progress: project.progress || 0,
          }))
          .sort((a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime())
          .slice(0, 5); // Show only 5 most recent projects
        setProjectTimelineData(projectTimelineData);

      } catch (error: any) {
        console.error('Error fetching dashboard data:', error);
        setError(error.message || 'Failed to load dashboard data');
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
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Typography 
        variant="h4" 
        gutterBottom
        sx={{
          fontWeight: 600,
          mb: 4,
          color: 'text.primary',
        }}
      >
        Dashboard Overview
      </Typography>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Projects"
            value={stats.totalProjects}
            subtitle={`${stats.activeProjects} Active Projects`}
            icon={<ProjectIcon sx={{ color: 'white', fontSize: 28 }} />}
            color={theme.palette.primary.main}
            trend={{ value: 12, label: 'vs last month' }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Tasks"
            value={stats.totalTasks}
            subtitle={`${stats.completedTasks} Completed Tasks`}
            icon={<TaskIcon sx={{ color: 'white', fontSize: 28 }} />}
            color={theme.palette.success.main}
            trend={{ value: 8, label: 'vs last month' }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Suppliers"
            value={stats.activeSuppliers}
            subtitle={`of ${stats.totalSuppliers} Total Suppliers`}
            icon={<SupplierIcon sx={{ color: 'white', fontSize: 28 }} />}
            color={theme.palette.info.main}
            trend={{ value: 5, label: 'vs last month' }}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Completion Rate"
            value={`${stats.totalProjects ? Math.round((stats.completedProjects / stats.totalProjects) * 100) : 0}%`}
            subtitle="Projects Completed"
            icon={<TrendingIcon sx={{ color: 'white', fontSize: 28 }} />}
            color={theme.palette.warning.main}
            trend={{ value: 15, label: 'vs last month' }}
          />
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        {/* Project Status Distribution */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Project Status Distribution
            </Typography>
            <Box height={300}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={projectStatusData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    label
                  >
                    {projectStatusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid>

        {/* Task Completion Trend */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Task Completion Trend
            </Typography>
            <Box height={300}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={taskTrendData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="completed"
                    stroke={theme.palette.success.main}
                    name="Completed Tasks"
                  />
                  <Line
                    type="monotone"
                    dataKey="pending"
                    stroke={theme.palette.warning.main}
                    name="Pending Tasks"
                  />
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid>

        {/* Project Timeline */}
        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Projects Timeline
            </Typography>
            <Box height={300}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={projectTimelineData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar
                    dataKey="progress"
                    fill={theme.palette.primary.main}
                    name="Progress (%)"
                  />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard; 