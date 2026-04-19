-- Mise à jour des traductions EN/NL pour les produits existants
-- À exécuter une seule fois sur la base de données de production

UPDATE product SET
    name_en = 'Rose Garden Bouquet',
    name_nl = 'Rozentuin Boeket',
    description_en = 'A generous bouquet of fresh roses in pink and white hues.',
    description_nl = 'Een royaal boeket verse rozen in roze en witte tinten.',
    short_description_en = 'Fresh roses in pastel shades with green foliage',
    short_description_nl = 'Verse rozen in pastelkleuren met groen gebladerte'
WHERE slug = 'bouquet-jardin-roses';

UPDATE product SET
    name_en = 'Eternal Spring Composition',
    name_nl = 'Eeuwige Lente Compositie',
    description_en = 'Tulips, ranunculus and daisies in a handmade ceramic vase.',
    description_nl = 'Tulpen, ranonkels en madelieven in een handgemaakt keramisch vaas.',
    short_description_en = 'Tulips, ranunculus and daisies in ceramic vase',
    short_description_nl = 'Tulpen, ranonkels en madelieven in keramisch vaas'
WHERE slug = 'composition-printemps-eternel';

UPDATE product SET
    name_en = 'Phalaenopsis Orchid',
    name_nl = 'Phalaenopsis Orchidee',
    description_en = 'An elegant phalaenopsis orchid in a decorative pot.',
    description_nl = 'Een elegante phalaenopsis orchidee in een decoratieve pot.',
    short_description_en = 'Orchid in decorative pot, long-lasting',
    short_description_nl = 'Orchidee in decoratieve pot, lang houdbaar'
WHERE slug = 'orchidee-phalaenopsis';

UPDATE product SET
    name_en = 'Timeless Bridal Bouquet',
    name_nl = 'Tijdloos Bruidsboeket',
    description_en = 'White peonies, garden roses and magnolia leaves.',
    description_nl = 'Witte pioenrozen, tuinrozen en magnolia bladeren.',
    short_description_en = 'White peonies and garden roses for your wedding',
    short_description_nl = 'Witte pioenrozen en tuinrozen voor uw bruiloft'
WHERE slug = 'bouquet-mariee-intemporel';

UPDATE product SET
    name_en = 'Wild & Natural Bouquet',
    name_nl = 'Wild & Natuurlijk Boeket',
    description_en = 'A rustic bouquet blending wildflowers and aromatic herbs.',
    description_nl = 'Een landelijk boeket met wilde bloemen en aromatische kruiden.',
    short_description_en = 'Wildflowers and herbs in a rustic spirit',
    short_description_nl = 'Wilde bloemen en kruiden in landelijke stijl'
WHERE slug = 'bouquet-sauvage-naturel';

UPDATE product SET
    name_en = 'Succulent Planter Box',
    name_nl = 'Vetplanten in Plantenbak',
    description_en = 'An assortment of colourful succulents in a natural wood planter.',
    description_nl = 'Een assortiment kleurrijke vetplanten in een houten plantenbak.',
    short_description_en = 'Mix of succulents in wooden planter',
    short_description_nl = 'Mix van vetplanten in houten plantenbak'
WHERE slug = 'succulentes-jardiniere';
