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
  Grid
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { supplierAPI } from '../services/api';
import { Supplier, SupplierStatus } from '../types';

const validationSchema = yup.object({
  supplierName: yup.string().required('Supplier name is required').min(3, 'Name must be at least 3 characters'),
  email: yup.string().email('Invalid email format').required('Email is required'),
  phoneNumber: yup.string().matches(/^\+?[0-9]{10,15}$/, 'Phone number must be valid').required('Phone number is required'),
  address: yup.string().required('Address is required'),
  category: yup.string().required('Category is required'),
  status: yup.string().required('Status is required'),
  description: yup.string().max(1000, 'Description must not exceed 1000 characters')
});

const categories = [
  'IT Services',
  'Construction',
  'Manufacturing',
  'Logistics',
  'Consulting',
  'Other'
];

const Suppliers: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [filterStatus, setFilterStatus] = useState<SupplierStatus | 'ALL'>('ALL');

  const fetchSuppliers = async () => {
    try {
      const response = await supplierAPI.getAllSuppliers(page, rowsPerPage);
      setSuppliers(response.data.data.content);
    } catch (error) {
      console.error('Error fetching suppliers:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSuppliers();
  }, [page, rowsPerPage]);

  const formik = useFormik({
    initialValues: {
      supplierName: '',
      email: '',
      phoneNumber: '',
      address: '',
      category: '',
      status: SupplierStatus.ACTIVE,
      description: ''
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        if (editingSupplier) {
          await supplierAPI.updateSupplier(editingSupplier.id, values);
        } else {
          await supplierAPI.createSupplier(values);
        }
        setOpenDialog(false);
        fetchSuppliers();
        formik.resetForm();
        setEditingSupplier(null);
      } catch (error) {
        console.error('Error saving supplier:', error);
      }
    }
  });

  const handleEdit = (supplier: Supplier) => {
    setEditingSupplier(supplier);
    formik.setValues({
      supplierName: supplier.supplierName,
      email: supplier.email,
      phoneNumber: supplier.phoneNumber,
      address: supplier.address,
      category: supplier.category,
      status: supplier.status,
      description: supplier.description || ''
    });
    setOpenDialog(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this supplier?')) {
      try {
        await supplierAPI.deleteSupplier(id);
        fetchSuppliers();
      } catch (error) {
        console.error('Error deleting supplier:', error);
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

  const getStatusColor = (status: SupplierStatus) => {
    switch (status) {
      case SupplierStatus.ACTIVE:
        return 'success';
      case SupplierStatus.INACTIVE:
        return 'default';
      case SupplierStatus.BLACKLISTED:
        return 'error';
      default:
        return 'default';
    }
  };

  const filteredSuppliers = filterStatus === 'ALL'
    ? suppliers
    : suppliers.filter(supplier => supplier.status === filterStatus);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Grid container spacing={2} alignItems="center" mb={3}>
        <Grid item xs={12} sm={6}>
          <Typography variant="h4">Suppliers Management</Typography>
        </Grid>
        <Grid item xs={12} sm={6} display="flex" justifyContent="flex-end" gap={2}>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Filter by Status</InputLabel>
            <Select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value as SupplierStatus | 'ALL')}
              label="Filter by Status"
            >
              <MenuItem value="ALL">All Statuses</MenuItem>
              {Object.values(SupplierStatus).map((status) => (
                <MenuItem key={status} value={status}>
                  {status}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => {
              setEditingSupplier(null);
              formik.resetForm();
              setOpenDialog(true);
            }}
          >
            Add Supplier
          </Button>
        </Grid>
      </Grid>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredSuppliers.map((supplier) => (
              <TableRow key={supplier.id}>
                <TableCell>{supplier.supplierName}</TableCell>
                <TableCell>{supplier.category}</TableCell>
                <TableCell>{supplier.email}</TableCell>
                <TableCell>{supplier.phoneNumber}</TableCell>
                <TableCell>
                  <Chip
                    label={supplier.status}
                    color={getStatusColor(supplier.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <IconButton onClick={() => handleEdit(supplier)} color="primary">
                    <EditIcon />
                  </IconButton>
                  <IconButton onClick={() => handleDelete(supplier.id)} color="error">
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
          <DialogTitle>{editingSupplier ? 'Edit Supplier' : 'Add New Supplier'}</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              margin="normal"
              name="supplierName"
              label="Supplier Name"
              value={formik.values.supplierName}
              onChange={formik.handleChange}
              error={formik.touched.supplierName && Boolean(formik.errors.supplierName)}
              helperText={formik.touched.supplierName && formik.errors.supplierName}
            />
            <TextField
              fullWidth
              margin="normal"
              name="email"
              label="Email"
              type="email"
              value={formik.values.email}
              onChange={formik.handleChange}
              error={formik.touched.email && Boolean(formik.errors.email)}
              helperText={formik.touched.email && formik.errors.email}
            />
            <TextField
              fullWidth
              margin="normal"
              name="phoneNumber"
              label="Phone Number"
              value={formik.values.phoneNumber}
              onChange={formik.handleChange}
              error={formik.touched.phoneNumber && Boolean(formik.errors.phoneNumber)}
              helperText={formik.touched.phoneNumber && formik.errors.phoneNumber}
            />
            <TextField
              fullWidth
              margin="normal"
              name="address"
              label="Address"
              value={formik.values.address}
              onChange={formik.handleChange}
              error={formik.touched.address && Boolean(formik.errors.address)}
              helperText={formik.touched.address && formik.errors.address}
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Category</InputLabel>
              <Select
                name="category"
                value={formik.values.category}
                onChange={formik.handleChange}
                error={formik.touched.category && Boolean(formik.errors.category)}
                label="Category"
              >
                {categories.map((category) => (
                  <MenuItem key={category} value={category}>
                    {category}
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
                {Object.values(SupplierStatus).map((status) => (
                  <MenuItem key={status} value={status}>
                    {status}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
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
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
            <Button type="submit" variant="contained" color="primary">
              {editingSupplier ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Suppliers; 