import React, { useState, useEffect } from 'react';
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
    Divider
} from '@mui/material';
import { useFormik } from 'formik';
import * as yup from 'yup';
import { authAPI } from '../services/api';

const validationSchema = yup.object({
    email: yup
        .string()
        .email('Enter a valid email')
        .required('Email is required'),
    password: yup
        .string()
        .min(6, 'Password should be of minimum 6 characters length')
        .required('Password is required')
});

const Login: React.FC = () => {
    const navigate = useNavigate();
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    // Check if user is already logged in
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            // Verify token validity here if needed
            navigate('/dashboard');
        }
    }, [navigate]);

    const formik = useFormik({
        initialValues: {
            email: '',
            password: ''
        },
        validationSchema,
        onSubmit: async (values) => {
            setError(null);
            setLoading(true);
            try {
                const response = await authAPI.login(values);
                if (response.data.success) {
                    const { token, user } = response.data.data;
                    localStorage.setItem('token', token);
                    localStorage.setItem('user', JSON.stringify(user));
                    navigate('/dashboard');
                } else {
                    setError(response.data.message || 'Login failed');
                }
            } catch (err: any) {
                if (err.response?.status === 403) {
                    setError('Invalid credentials. Please check your email and password.');
                } else if (err.response?.status === 401) {
                    setError('Your session has expired. Please login again.');
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                } else {
                    setError(err.response?.data?.message || 'An error occurred during login');
                }
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
                        Admin Portal
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                        Sign in to your account
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
                            id="email"
                            name="email"
                            label="Email Address"
                            autoComplete="email"
                            autoFocus
                            value={formik.values.email}
                            onChange={formik.handleChange}
                            error={formik.touched.email && Boolean(formik.errors.email)}
                            helperText={formik.touched.email && formik.errors.email}
                            disabled={loading}
                        />
                        <TextField
                            margin="normal"
                            fullWidth
                            name="password"
                            label="Password"
                            type="password"
                            id="password"
                            autoComplete="current-password"
                            value={formik.values.password}
                            onChange={formik.handleChange}
                            error={formik.touched.password && Boolean(formik.errors.password)}
                            helperText={formik.touched.password && formik.errors.password}
                            disabled={loading}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Sign In'}
                        </Button>

                        <Divider sx={{ my: 2 }}>OR</Divider>

                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, width: '100%' }}>
                            <Button
                                component={RouterLink}
                                to="/register/admin"
                                variant="outlined"
                                fullWidth
                                disabled={loading}
                            >
                                Register as Admin
                            </Button>
                            <Button
                                component={RouterLink}
                                to="/register/supplier"
                                variant="outlined"
                                fullWidth
                                disabled={loading}
                            >
                                Register as Supplier
                            </Button>
                        </Box>

                        <Box sx={{ mt: 2, textAlign: 'center' }}>
                            <Link component={RouterLink} to="/forgot-password" variant="body2">
                                Forgot password?
                            </Link>
                        </Box>
                    </Box>
                </Paper>
            </Box>
        </Container>
    );
};

export default Login; 