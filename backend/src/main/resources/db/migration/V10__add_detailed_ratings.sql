-- Migration V8: Add detailed ratings to reviews table
-- Add new columns for detailed rating system

ALTER TABLE reviews 
ADD COLUMN expertise_rating INT CHECK (expertise_rating >= 1 AND expertise_rating <= 5),
ADD COLUMN communication_rating INT CHECK (communication_rating >= 1 AND communication_rating <= 5),
ADD COLUMN punctuality_rating INT CHECK (punctuality_rating >= 1 AND punctuality_rating <= 5),
ADD COLUMN care_rating INT CHECK (care_rating >= 1 AND care_rating <= 5),
ADD COLUMN overall_rating INT CHECK (overall_rating >= 1 AND overall_rating <= 5),
ADD COLUMN is_recommended BOOLEAN DEFAULT FALSE,
ADD COLUMN is_anonymous BOOLEAN DEFAULT FALSE;

-- Migrate existing data (if any) - set all detailed ratings to the original rating value
UPDATE reviews SET 
    overall_rating = rating,
    expertise_rating = rating,
    communication_rating = rating,
    punctuality_rating = rating,
    care_rating = rating,
    is_recommended = true
WHERE overall_rating IS NULL;

-- Make new columns NOT NULL after migration
ALTER TABLE reviews 
MODIFY COLUMN expertise_rating INT NOT NULL,
MODIFY COLUMN communication_rating INT NOT NULL,
MODIFY COLUMN punctuality_rating INT NOT NULL,
MODIFY COLUMN care_rating INT NOT NULL,
MODIFY COLUMN overall_rating INT NOT NULL,
MODIFY COLUMN is_recommended BOOLEAN NOT NULL,
MODIFY COLUMN is_anonymous BOOLEAN NOT NULL;

-- Add indexes for performance
CREATE INDEX idx_reviews_overall_rating ON reviews(overall_rating);
CREATE INDEX idx_reviews_is_recommended ON reviews(is_recommended);
CREATE INDEX idx_reviews_is_anonymous ON reviews(is_anonymous);