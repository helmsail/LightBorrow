-- ============================================================================
-- LightBorrow 示例数据（Spring Boot 自动执行）
-- PostgreSQL 兼容语法，可重复执行
-- ============================================================================

INSERT INTO asset (id, code, name, description, status)
VALUES
    (1, 'IT-001', 'MacBook Pro 16" M3 Max', 'Apple MacBook Pro 16英寸 M3 Max 芯片 / 64GB 内存 / 1TB 固态硬盘 / 深空黑色', 'available'),
    (2, 'IT-002', 'MacBook Pro 14" M3 Pro', 'Apple MacBook Pro 14英寸 M3 Pro 芯片 / 18GB 内存 / 512GB 固态硬盘 / 银色', 'available'),
    (3, 'IT-003', 'ThinkPad X1 Carbon Gen 11', '联想 ThinkPad X1 Carbon 第11代 / i7-1365U / 16GB 内存 / 512GB 固态硬盘 / 14英寸 2.8K OLED', 'available'),
    (4, 'IT-004', 'ThinkPad P16s Gen 2', '联想 ThinkPad P16s 工作站 / i7-1370P / 32GB 内存 / 1TB 固态硬盘 / NVIDIA RTX A500', 'available'),
    (5, 'IT-005', 'Dell UltraSharp U2723QE 显示器', '戴尔 27英寸 4K IPS Black 显示器 / USB-C 90W 供电 / 内置 KVM', 'available'),
    (6, 'IT-006', 'Dell UltraSharp U4323QE 显示器', '戴尔 42.5英寸 4K IPS 显示器 / USB-C 90W 供电 / 内置 KVM / 多画面', 'available'),
    (7, 'IT-007', 'Logitech MX Keys Mini 键盘', '罗技 MX Keys Mini 无线键盘 / 蓝牙+优联 / 深空灰色', 'available'),
    (8, 'IT-008', 'Logitech MX Master 3S 鼠标', '罗技 MX Master 3S 无线鼠标 / 8K DPI / USB-C 充电 / 深空灰色', 'available'),
    (9, 'IT-009', 'iPad Pro 12.9" M2', 'Apple iPad Pro 12.9英寸 M2 芯片 / 256GB / 深空灰色 / 含 Apple Pencil 2代', 'borrowed'),
    (10, 'IT-010', 'AirPods Pro 2', 'Apple AirPods Pro 第二代 / USB-C 充电盒 / 主动降噪', 'available')
ON CONFLICT (id) DO NOTHING;
