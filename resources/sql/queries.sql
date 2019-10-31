-- :name create-bond! :! :n
-- :doc creates a new bond record
INSERT INTO bond
            (description,
            updated,
            principal,
            deposit,
            interest_rate,
            term_years)
VALUES      (:description,
            LOCALTIMESTAMP(),
            :principal,
            :deposit,
            :interest_rate,
            :term_years)

-- :name update-bond! :! :n
-- :doc updates an existing bond record
UPDATE      bond
SET         description         = :description,
            updated             = LOCALTIMESTAMP(),
            principal           = :principal,
            deposit             = :deposit,
            interest_rate       = :interest_rate,
            term_years          = :term_years
WHERE       id = :id

-- :name get-bond :? :1
-- :doc retrieves a bond record given the id
SELECT      *
FROM        bond
WHERE       id = :id

-- :name get-bonds :? :*
-- :doc selects all available bonds
SELECT      id,
            description,
            FORMATDATETIME(updated, 'Y-MM-dd HH:mm:ss') updated,
            principal,
            deposit,
            interest_rate,
            term_years
FROM        bond
ORDER BY    updated DESC
