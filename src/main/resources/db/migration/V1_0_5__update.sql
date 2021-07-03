ALTER TABLE `steve_settings` CHANGE COLUMN `flow_enabled` `webhook_enabled` BOOLEAN DEFAULT FALSE;
ALTER TABLE `steve_settings` CHANGE COLUMN `flow` `webhook` VARCHAR(255) DEFAULT NULL;