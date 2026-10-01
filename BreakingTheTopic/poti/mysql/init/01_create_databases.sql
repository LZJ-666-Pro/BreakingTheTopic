SET NAMES utf8mb4;
-- poti 项目建库脚本(MySQL 容器首次启动自动执行)
-- poti_admin 由 09_poti_admin.sql 自行创建,此处不重复

CREATE DATABASE IF NOT EXISTS `poti_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `poti_question` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `poti_practice` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `poti_favorite` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `poti_wrongbook` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `poti_interview` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
