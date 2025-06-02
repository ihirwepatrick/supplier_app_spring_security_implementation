import React, { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import {
    Box,
    Button,
    Container,
    TextField,
    Typography,
    Paper,
    Alert,
    CircularProgress,
    Link,
    FormControl,
    InputLabel,
    Select,
    MenuItem
} from '@mui/material';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { authAPI } from '../services/api';

const categories = [
    'IT Services',
    'Construction',
    'Manufacturing',
    'Logistics',
    'Consulting',
    'Other'
];

const validationSchema = yup.object({
    supplierName: yup
        .string()
        .required('Supplier name is required')
        .min(3, 'Supplier name must be at least 3 characters'),
    email: yup
        .string()
        .email('Enter a valid email')
        .required('Email is required'),
    phoneNumber: yup
        .string()
        .matches(/^\+?[0-9]{10,15}$/, 'Phone number must be valid')
        .required('Phone number is required'),
    address: yup
        .string()
        .required('Address is required'),
    category: yup
        .string()
        .required('Category is required'),
    password: yup
        .string()
        .min(6, 'Password should be of minimum 6 characters length')
        .required('Password is required'),
    confirmPassword: yup
        .string()
        .oneOf([yup.ref('password')], 'Passwords must match')
        .required('Confirm password is required'),
    description: yup
        .string()
        .max(1000, 'Description must not exceed 1000 characters')
});

const RegisterSupplier: React.FC = () => {
    const navigate = useNavigate();
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const formik = useFormik({
        initialValues: {
            supplierName: '',
            email: '',
            phoneNumber: '',
            address: '',
            category: '',
            password: '',
            confirmPassword: '',
            description: ''
        },
        validationSchema,
        onSubmit: async (values) => {
            setError(null);
            setLoading(true);
            try {
                const { confirmPassword, ...registrationData } = values;
                const response = await authAPI.registerSupplier(registrationData);
                if (response.data.success) {
                    // Optionally auto-login after registration
                    const loginResponse = await authAPI.login({
                        email: values.email,
                        password: values.password
                    });
                    if (loginResponse.data.success) {
                        const { token, user } = loginResponse.data.data;
                        localStorage.setItem('token', token);
                        localStorage.setItem('user', JSON.stringify(user));
                        navigate('/dashboard');
                    }
                } else {
                    setError(response.data.message || 'Registration failed');
                }
            } catch (err: any) {
                setError(err.response?.data?.message || 'An error occurred during registration');
            } finally {
                setLoading(false);
            }
        }
    });

    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center'
                }}
            >
                <Paper
                    elevation={3}
                    sx={{
                        padding: 4,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        width: '100%'
                    }}
                >
                    <Typography component="h1" variant="h5" gutterBottom>
                        Register as Supplier
                    </Typography>
                    
                    {error && (
                        <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
                            {error}
                        </Alert>
                    )}
                    
                    <Box component="form" onSubmit={formik.handleSubmit} sx={{ mt: 1, width: '100%' }}>
                        <TextField
                            margin="normal"
                            fullWidth
                            id="supplierName"
                            name="supplierName"
                            label="Supplier Name"
                            autoComplete="organization"
                            autoFocus
                            value={formik.values.supplierName}
                            onChange={formik.handleChange}
                            error={formik.touched.supplierName && Boolean(formik.errors.supplierName)}
                            helperText={formik.touched.supplierName && formik.errors.supplierName}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            id="email"
                            name="email"
                            label="Email Address"
                            autoComplete="email"
                            value={formik.values.email}
                            onChange={formik.handleChange}
                            error={formik.touched.email && Boolean(formik.errors.email)}
                            helperText={formik.touched.email && formik.errors.email}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            id="phoneNumber"
                            name="phoneNumber"
                            label="Phone Number"
                            autoComplete="tel"
                            value={formik.values.phoneNumber}
                            onChange={formik.handleChange}
                            error={formik.touched.phoneNumber && Boolean(formik.errors.phoneNumber)}
                            helperText={formik.touched.phoneNumber && formik.errors.phoneNumber}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            id="address"
                            name="address"
                            label="Address"
                            autoComplete="street-address"
                            value={formik.values.address}
                            onChange={formik.handleChange}
                            error={formik.touched.address && Boolean(formik.errors.address)}
                            helperText={formik.touched.address && formik.errors.address}
                            disabled={loading}
                        />
                        <FormControl fullWidth margin="normal">
                            <InputLabel>Category</InputLabel>
                            <Select
                                name="category"
                                value={formik.values.category}
                                onChange={formik.handleChange}
                                error={formik.touched.category && Boolean(formik.errors.category)}
                                label="Category"
                                disabled={loading}
                            >
                                {categories.map((category) => (
                                    <MenuItem key={category} value={category}>
                                        {category}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <TextField
                            margin="normal"
                            fullWidth
                            name="password"
                            label="Password"
                            type="password"
                            id="password"
                            autoComplete="new-password"
                            value={formik.values.password}
                            onChange={formik.handleChange}
                            error={formik.touched.password && Boolean(formik.errors.password)}
                            helperText={formik.touched.password && formik.errors.password}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            name="confirmPassword"
                            label="Confirm Password"
                            type="password"
                            id="confirmPassword"
                            autoComplete="new-password"
                            value={formik.values.confirmPassword}
                            onChange={formik.handleChange}
                            error={formik.touched.confirmPassword && Boolean(formik.errors.confirmPassword)}
                            helperText={formik.touched.confirmPassword && formik.errors.confirmPassword}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            name="description"
                            label="Description"
                            multiline
                            rows={4}
                            value={formik.values.description}
                            onChange={formik.handleChange}
                            error={formik.touched.description && Boolean(formik.errors.description)}
                            helperText={formik.touched.description && formik.errors.description}
                            disabled={loading}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Register'}
                        </Button>
                        
                        <Box sx={{ textAlign: 'center' }}>
                            <Link component={RouterLink} to="/login" variant="body2">
                                Already have an account? Sign in
                            </Link>
                        </Box>
                    </Box>
                </Paper>
            </Box>
        </Container>
    );
};

export default RegisterSupplier; 