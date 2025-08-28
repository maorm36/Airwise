

import * as AuthApi from './AuthApi';
import * as SiteApi from './SiteApi';
import * as RoomApi from './RoomApi';
import * as NotificationApi from './NotificationApi';
import * as ACUnitApi from './ACUnitApi';
import * as TaskApi from './TaskApi';
import userApi from './userApi';
import { Settings } from '@mui/icons-material';
import SettingsApi from './SettingsApi';
import TenantApi from './TenantApi';
import commandApi from './commandApi';

export default {
  ...AuthApi,
  ...SiteApi,
  ...RoomApi,
  ...NotificationApi,
  ...ACUnitApi,
  ...TaskApi,
  ...userApi,
  ...SettingsApi,
  ...TenantApi,
  ...commandApi
};