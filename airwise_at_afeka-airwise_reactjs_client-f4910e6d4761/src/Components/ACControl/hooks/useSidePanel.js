import { useState } from 'react';

export const useSidePanel = () => {
    const [panelOpen, setPanelOpen] = useState(false);
    const [panelContent, setPanelContent] = useState(null);
    const [panelTitle, setPanelTitle] = useState('');

    const openPanel = (form, title) => {
        setPanelContent(form);
        setPanelTitle(title);
        setPanelOpen(true);
    };

    const closePanel = () => {
        setPanelOpen(false);
    };

    return {
        panelOpen,
        panelContent,
        panelTitle,
        openPanel,
        closePanel
    };
};