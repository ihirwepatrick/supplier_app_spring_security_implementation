import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
  MenuItem,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  Chip,
  Alert
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { taskAPI, projectAPI, supplierAPI } from '../services/api';
import { Task, TaskStatus, TaskPriority, Project, Supplier } from '../types';

const validationSchema = yup.object({
  name: yup.string().required('Task name is required').min(3, 'Name must be at least 3 characters'),
  description: yup.string().max(1000, 'Description must not exceed 1000 characters'),
  status: yup.string().required('Status is required'),
  priority: yup.string().required('Priority is required'),
  startDate: yup.date().nullable(),
  dueDate: yup.date().nullable().min(yup.ref('startDate'), 'Due date must be after start date'),
  projectId: yup.number().required('Project is required'),
  assignedToId: yup.number().nullable()
});

const Tasks: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [error, setError] = useState<string | null>(null);

  const fetchTasks = async () => {
    try {
      console.log('Fetching tasks...');
      const response = await taskAPI.getAllTasks(page, rowsPerPage);
      console.log('Tasks API Response:', response.data);
      if (response.data.success) {
        setTasks(response.data.data.content);
      } else {
        console.error('API returned success: false', response.data.message);
        setError(response.data.message);
      }
    } catch (error: any) {
      console.error('Error fetching tasks:', error);
      console.error('Error details:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      });
      setError(error.response?.data?.message || 'Failed to fetch tasks');
    } finally {
      setLoading(false);
    }
  };

  const fetchProjects = async () => {
    try {
      const response = await projectAPI.getAllProjects(0, 1000);
      setProjects(response.data.data.content);
    } catch (error) {
      console.error('Error fetching projects:', error);
    }
  };

  const fetchSuppliers = async () => {
    try {
      const response = await supplierAPI.getAllSuppliers(0, 1000);
      setSuppliers(response.data.data.content);
    } catch (error) {
      console.error('Error fetching suppliers:', error);
    }
  };

  useEffect(() => {
    fetchTasks();
    fetchProjects();
    fetchSuppliers();
  }, [page, rowsPerPage]);

  const formik = useFormik({
    initialValues: {
      name: '',
      description: '',
      status: TaskStatus.PENDING,
      priority: TaskPriority.MEDIUM,
      startDate: '',
      dueDate: '',
      projectId: '',
      assignedToId: ''
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        if (editingTask) {
          await taskAPI.updateTask(editingTask.id, values);
        } else {
          await taskAPI.createTask(Number(values.projectId), values);
        }
        setOpenDialog(false);
        fetchTasks();
        formik.resetForm();
        setEditingTask(null);
      } catch (error) {
        console.error('Error saving task:', error);
      }
    }
  });

  const handleEdit = (task: Task) => {
    setEditingTask(task);
    formik.setValues({
      name: task.name,
      description: task.description || '',
      status: task.status,
      priority: task.priority,
      startDate: task.startDate || '',
      dueDate: task.dueDate || '',
      projectId: task.project.id.toString(),
      assignedToId: task.assignedTo?.id.toString() || ''
    });
    setOpenDialog(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await taskAPI.deleteTask(id);
        fetchTasks();
      } catch (error) {
        console.error('Error deleting task:', error);
      }
    }
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getStatusColor = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.PENDING:
        return 'default';
      case TaskStatus.IN_PROGRESS:
        return 'primary';
      case TaskStatus.COMPLETED:
        return 'success';
      case TaskStatus.BLOCKED:
        return 'error';
      default:
        return 'default';
    }
  };

  const getPriorityColor = (priority: TaskPriority) => {
    switch (priority) {
      case TaskPriority.LOW:
        return 'success';
      case TaskPriority.MEDIUM:
        return 'primary';
      case TaskPriority.HIGH:
        return 'warning';
      case TaskPriority.URGENT:
        return 'error';
      default:
        return 'default';
    }
  };

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
        <Button 
          variant="contained" 
          onClick={() => {
            setError(null);
            fetchTasks();
          }}
        >
          Retry
        </Button>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Tasks Management</Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => {
            setEditingTask(null);
            formik.resetForm();
            setOpenDialog(true);
          }}
        >
          Add Task
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Project</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Priority</TableCell>
              <TableCell>Assigned To</TableCell>
              <TableCell>Due Date</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {tasks.map((task) => (
              <TableRow key={task.id}>
                <TableCell>{task.name}</TableCell>
                <TableCell>{task.project?.name ?? 'Unknown Project'}</TableCell>
                <TableCell>
                  <Chip
                    label={task.status}
                    color={getStatusColor(task.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={task.priority}
                    color={getPriorityColor(task.priority)}
                    size="small"
                  />
                </TableCell>
                <TableCell>{task.assignedTo?.supplierName ?? 'Unassigned'}</TableCell>
                <TableCell>{task.dueDate ?? 'Not set'}</TableCell>
                <TableCell>
                  <IconButton onClick={() => handleEdit(task)} color="primary">
                    <EditIcon />
                  </IconButton>
                  <IconButton onClick={() => handleDelete(task.id)} color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={-1}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </TableContainer>

      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <form onSubmit={formik.handleSubmit}>
          <DialogTitle>{editingTask ? 'Edit Task' : 'Add New Task'}</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              margin="normal"
              name="name"
              label="Task Name"
              value={formik.values.name}
              onChange={formik.handleChange}
              error={formik.touched.name && Boolean(formik.errors.name)}
              helperText={formik.touched.name && formik.errors.name}
            />
            <TextField
              fullWidth
              margin="normal"
              name="description"
              label="Description"
              multiline
              rows={4}
              value={formik.values.description}
              onChange={formik.handleChange}
              error={formik.touched.description && Boolean(formik.errors.description)}
              helperText={formik.touched.description && formik.errors.description}
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Project</InputLabel>
              <Select
                name="projectId"
                value={formik.values.projectId}
                onChange={formik.handleChange}
                error={formik.touched.projectId && Boolean(formik.errors.projectId)}
                label="Project"
              >
                {projects.map((project) => (
                  <MenuItem key={project.id} value={project.id}>
                    {project.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                name="status"
                value={formik.values.status}
                onChange={formik.handleChange}
                error={formik.touched.status && Boolean(formik.errors.status)}
                label="Status"
              >
                {Object.values(TaskStatus).map((status) => (
                  <MenuItem key={status} value={status}>
                    {status}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Priority</InputLabel>
              <Select
                name="priority"
                value={formik.values.priority}
                onChange={formik.handleChange}
                error={formik.touched.priority && Boolean(formik.errors.priority)}
                label="Priority"
              >
                {Object.values(TaskPriority).map((priority) => (
                  <MenuItem key={priority} value={priority}>
                    {priority}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Assigned To</InputLabel>
              <Select
                name="assignedToId"
                value={formik.values.assignedToId}
                onChange={formik.handleChange}
                label="Assigned To"
              >
                <MenuItem value="">Unassigned</MenuItem>
                {suppliers.map((supplier) => (
                  <MenuItem key={supplier.id} value={supplier.id}>
                    {supplier.supplierName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              fullWidth
              margin="normal"
              name="startDate"
              label="Start Date"
              type="date"
              value={formik.values.startDate}
              onChange={formik.handleChange}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              fullWidth
              margin="normal"
              name="dueDate"
              label="Due Date"
              type="date"
              value={formik.values.dueDate}
              onChange={formik.handleChange}
              InputLabelProps={{ shrink: true }}
              error={formik.touched.dueDate && Boolean(formik.errors.dueDate)}
              helperText={formik.touched.dueDate && formik.errors.dueDate}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button type="submit" variant="contained" color="primary">
              {editingTask ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>