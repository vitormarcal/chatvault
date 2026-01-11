-- Create chat table
CREATE TABLE IF NOT EXISTS chat (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    external_id VARCHAR(255),
    bucket VARCHAR(255) NOT NULL,
    CONSTRAINT chat_external_id_key UNIQUE (external_id)
);

-- Create indexes on chat table
CREATE INDEX IF NOT EXISTS idx_chat_external_id ON chat(external_id);
CREATE INDEX IF NOT EXISTS idx_chat_name ON chat(name);

-- Create message table
CREATE TABLE IF NOT EXISTS message (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    author VARCHAR(255) NOT NULL,
    author_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content TEXT NOT NULL,
    attachment_path VARCHAR(255),
    attachment_name VARCHAR(255),
    chat_id BIGINT NOT NULL,
    external_id VARCHAR(255),
    CONSTRAINT message_external_id_key UNIQUE (external_id),
    CONSTRAINT fk_message_chat_id FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE
);

-- Create indexes on message table
CREATE INDEX IF NOT EXISTS idx_message_chat_id ON message(chat_id);
CREATE INDEX IF NOT EXISTS idx_message_external_id ON message(external_id);
CREATE INDEX IF NOT EXISTS idx_message_created_at ON message(created_at);
