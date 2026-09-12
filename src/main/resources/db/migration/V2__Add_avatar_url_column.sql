-- Profile avatar public URL (Contabo volume / local disk behind /avatars)
ALTER TABLE users
ADD COLUMN avatar_url VARCHAR(512);

