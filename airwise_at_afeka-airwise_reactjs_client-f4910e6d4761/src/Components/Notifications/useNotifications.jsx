// import { useState, useEffect, useMemo, useCallback } from 'react';
// import { fetchNotifications } from '../../WebApi/WebApi.js';

// export default function useNotifications({ search, locationFilter, startDate, endDate, autoRefresh }) {
//   const [rows, setRows] = useState([]);
//   const [loading, setLoading] = useState(false);

//   const reload = useCallback(async () => {
//     setLoading(true);
//     try {
//       const data = await fetchNotifications({ search, location: locationFilter, startDate, endDate });
//       setRows(data);
//     } catch (err) {
//       console.error(err);
//     } finally {
//       setLoading(false);
//     }
//   }, [search, locationFilter, startDate, endDate]);

//   useEffect(() => {
//     reload();
//     if (autoRefresh) {
//       const iv = setInterval(reload, 30000);
//       return () => clearInterval(iv);
//     }
//   }, [reload, autoRefresh]);

//   const counts = useMemo(() => rows.reduce((acc, r) => {
//     if (r.status === 'Unread') acc.unread++;
//     if (r.type === 'Info') acc.info++;
//     if (r.type === 'Warning') acc.warning++;
//     if (r.type === 'Maintenance') acc.maintenance++;
//     return acc;
//   }, { unread: 0, info: 0, warning: 0, maintenance: 0 }), [rows]);

//   return { rows, loading, counts, reload };
// }