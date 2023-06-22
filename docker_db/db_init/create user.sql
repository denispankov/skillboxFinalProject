create database search_engine;
CREATE USER search_engine WITH PASSWORD 'search_engine';
GRANT ALL ON DATABASE search_engine TO search_engine;
ALTER DATABASE search_engine OWNER TO search_engine;