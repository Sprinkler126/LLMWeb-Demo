-- 更新Python合规检测接口地址
-- 原地址：http://localhost:5000/check_compliance
-- 新地址：http://localhost:5000/api/compliance/check
UPDATE sys_config 
SET config_value = 'http://localhost:5000/api/compliance/check'
WHERE config_key = 'python.compliance.endpoint';
