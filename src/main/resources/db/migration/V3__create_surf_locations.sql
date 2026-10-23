CREATE TABLE surf_locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    description VARCHAR(2000),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_surf_locations_active ON surf_locations (active);

INSERT INTO surf_locations (name, region, description, latitude, longitude, active) VALUES
('Arugam Bay', 'Eastern Province', 'The classic Sri Lankan right-hander, a world-famous point break and the heart of the Arugam Bay surf scene.', 6.8402, 81.8358, true),
('Whisky Point', 'Eastern Province', 'A fun, forgiving beach break at the northern end of Arugam Bay, popular with beginners and longboarders.', 6.8422, 81.8368, true),
('Peanut Farm', 'Eastern Province', 'A mellow beach break north of Arugam Bay, known for its relaxed, uncrowded waves.', 6.8512, 81.8290, true),
('Pottuvil Point', 'Eastern Province', 'A long, powerful right-hand point break favoured by intermediate and advanced surfers.', 6.8699, 81.8356, true),
('Okanda', 'Eastern Province', 'A remote, exposed beach break near Kumana, best suited to experienced surfers chasing bigger swell.', 6.6056, 81.7962, true);
