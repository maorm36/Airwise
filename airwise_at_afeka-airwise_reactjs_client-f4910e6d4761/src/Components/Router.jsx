import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import ACControlScreen from './ACControl/ACControlScreen';
import HomeScreen from './Home/HomeScreen';
import SettingsScreen from './SettingsScreen';
import LogInPage from './Login/LogInPage';
import RegisterPage from "./Register/RegisterPage";

const ProtectedRoute = ({ children }) => {
    const isLoggedIn = localStorage.getItem('user');
    return isLoggedIn ? children : <Navigate to="/login" replace />;
};

const AuthRoute = ({ children }) => {
    const isLoggedIn = localStorage.getItem('user');
    return isLoggedIn ? <Navigate to="/home" replace /> : children;
};

export default function Router() {
    const isLoggedIn = localStorage.getItem('user');

    return (
        <Routes>
            <Route path="/" element={
                isLoggedIn ? <Navigate to="/home" replace /> : <Navigate to="/login" replace />
            } />

            <Route path="/login" element={
                <AuthRoute>
                    <LogInPage />
                </AuthRoute>
            } />
            <Route path="/register" element={
                <AuthRoute>
                    <RegisterPage />
                </AuthRoute>
            } />

            <Route path="/home" element={
                <ProtectedRoute>
                    <HomeScreen />
                </ProtectedRoute>
            } />
            <Route path="/ac" element={
                <ProtectedRoute>
                    <ACControlScreen />
                </ProtectedRoute>
            } />
            <Route path="/settings" element={
                <ProtectedRoute>
                    <SettingsScreen />
                </ProtectedRoute>
            } />
        </Routes>
    );
}