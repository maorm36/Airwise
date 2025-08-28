import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Button,
} from '@mui/material';

export default function UpdateSiteForm({ site, onSubmit, onCancel }) {
    const [siteName, setSiteName] = useState('');
    const [inSite, setInSite] = useState(site?.objectDetails?.inSite);

    useEffect(() => {
        if (site) {
            setSiteName(site.alias || '');
        }
    }, [site]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (siteName.trim()) {
            let objectDetails = { ...site?.objectDetails }; // This is fine
            objectDetails.inSite = inSite;

            if (site) {
                site.objectDetails = {
                    ...objectDetails,
                    siteName: siteName.trim()
                };

                onSubmit(site, siteName.trim(), inSite);
            } else {
                throw new Error('Site object is required for update');
            }
        }
    };

    const handleCancel = () => {
        setSiteName(site?.alias || '');
        setInSite(site?.objectDetails?.inSite || true);
        if (onCancel) {
            onCancel();
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
                label="Site Name"
                fullWidth
                required
                value={siteName}
                onChange={(e) => setSiteName(e.target.value)}
                placeholder="Enter site name"
            />

            <FormControl fullWidth margin="normal">
                <InputLabel>Are you in This Site ?</InputLabel>
                <Select
                    value={inSite}
                    onChange={(e) => setInSite(e.target.value)}
                    label="Are you in This Site ?"
                >
                    <MenuItem value={true}>Yes</MenuItem>
                    <MenuItem value={false}>No</MenuItem>
                </Select>
            </FormControl>

            <Box sx={{ display: 'flex', gap: 1 }}>
                <Button type="submit" variant="contained" disabled={!siteName.trim()}>
                    Update
                </Button>
                <Button type="button" variant="outlined" onClick={handleCancel}>
                    Cancel
                </Button>
            </Box>
        </Box>
    );
}