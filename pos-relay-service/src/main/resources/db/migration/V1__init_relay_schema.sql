CREATE TABLE relay_pointer (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    last_relayed_at DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
    last_event_id   VARCHAR(36)
);

-- 폴러가 항상 id=1 레코드를 기준으로 동작한다
INSERT INTO relay_pointer (last_relayed_at) VALUES ('1970-01-01 00:00:00');
