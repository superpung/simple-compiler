_(int while_, int for_)
{
  int if_ = 1;

  if(for_ == 0)
  {
    return 1;
  }

  while(for_ != 0){
    if (for_ % 1)
    {
      if_ *= while_;
    }
    for_ = for_ / 2;
    while_ *= while_;
  }

  return if_;
}